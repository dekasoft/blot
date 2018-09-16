package com.dekagames.blot.algorithm;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;

/**
 * класс растрового контура. используется как промежуточное звено при переходе от
 * растрового пятна к векторному контуру
 */
public class RasterContour {
    private ArrayList<PixelXY> pixels;

    private RasterContour(){
        pixels = new ArrayList<>();
    }

    public void testDraw(BufferedImage img, int color) {
        // преобразуем изображение в массив пикселей
        final int[] data = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int w = img.getWidth();
        //int h = img.getHeight();

        int c1 = 0xFF/50;
        int col;
        for (PixelXY p :pixels){
            // цвет в зависимости от cosfi
//            int c2 = (int)(Math.abs(p.deltaDelta) * c1);

            if (p.isCorner)          // угловая точка
                col = 0xFF000000;
//            else if (p.fratio < 0.95)                                // обычная точка
//                col = color;
            else
                col = 0xFF00FF00;//color;

            data[p.y*w+p.x] = col;
        }
    }


    /** метод принимает на вход изображение и цвет, для которого необходимо найти контуры
     и выделяет из него связанные контуры, состоящие из упорядоченных растровых точек,
     возвращая их в виде ArrayList-а
     */
    public static ArrayList<RasterContour> get_contours_from_points(BufferedImage img, Color color){
        ArrayList<RasterContour> result = new ArrayList<>();

        // найдем все контурные точки как карту boolean-ов
        boolean[][] points = find_contour_points(img, color);

        int img_w = img.getWidth();
        int img_h = img.getHeight();

        // массив в котором будем помечать уже обработанные точки
        boolean[][] done_map = new boolean[img_w][img_h];

        // пробежим по всей карте, в поисках нового контура
        for (int y = 0; y < img_h; y++){
            for (int x = 0; x < img_w; x++){
                if (!done_map[x][y]) {      // если ячейка еще не обработана
                    if (points[x][y]) {     // найден новый контур
                        RasterContour cntr = new RasterContour();
                        PixelXY p = new PixelXY(x,y);

                        while (p != null) {
                            cntr.pixels.add(p);
                            p = find_next_contour_point(p, img_w, img_h, points, done_map);
                        }
                        // невозможно маленькие контуры просто не добавляем
                        if (cntr.pixels.size() > 4)
                            result.add(cntr);

                    }
                }
            }
        }

        return result;
    }


    /**
     * Преобразует растровый контур в векторный
     * @return векторный контур
     */

    public VContour toVContour(BufferedImage img, int brushSize){
        VContour result = new VContour();

        final float cornerRatio = 0.5f;     // соотношение при котором точка контура считается угловой
        final float cornerCos   = -0.8f;    // точки с косинусом меньше - не рассматриваем

        final int[] data = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int w = img.getWidth();
        int h = img.getHeight();

        // сразу уберем каждую вторую точку
        for (int i = pixels.size()-1; i>=0; i-=2){
            pixels.remove(i);
        }

        // посчитаем косинус угла при каждой точке
        int size = pixels.size();
        int step = 1+brushSize/3;

        for (int i = 0; i < size; i++){
            // i - предудущий сосед
            // j - точка, для которой будем делать расчет
            // k - последующий сосед
            int j = i + step;
            while (j >= size) j = j - size;   // закольцуем

            int k = j + step;
            while (k >= size) k = k - size;

            // косинус угла будем рассчитывать по формуле скалярного произведения в координатной форме
            // cosfi = (x1*x2 + y1*y2)/(|x1,y1|*|x2,y2|) , где (x1,y1) - координаты вектора (ij),
            // (x2,y2) - координаты вектора (k,j)
            int x1 = pixels.get(i).x - pixels.get(j).x;
            int y1 = pixels.get(i).y - pixels.get(j).y;
            int x2 = pixels.get(k).x - pixels.get(j).x;
            int y2 = pixels.get(k).y - pixels.get(j).y;
            float len1 = (float)Math.sqrt(x1*x1 + y1*y1);
            float len2 = (float)Math.sqrt(x2*x2 + y2*y2);

            pixels.get(j).cosfi = (x1*x2 + y1*y2)/(len1*len2);
        }


        // детектор SUSAN
        int radius = 1+brushSize/2;       // радиус круга для просмотра. Должен зависеть от размера кисти которым рисовалось пятно (?)
        size = pixels.size();

        // пробежим по всем пикселям контура, будем считать соотношение закрашенных и незакрашенных пикселей
        // в круге с центром в текущем пикселе/
        // предварительно отберем по косинусу
        for (int i = 0; i < size; i++){
            if (pixels.get(i).cosfi < cornerCos)
                continue;

            int x = pixels.get(i).x;
            int y = pixels.get(i).y;

            float f_full = 0.000001f;       // вес закрашенных пикселей
            float f_empty = 0.000001f;      // вес пустых пикселей

            // пробежим по квадратному окну, окружающему исследуемый пиксель
            for (int k = y-radius; k <= y + radius; k++){
                for (int j = x-radius; j <= x + radius; j++){
                    // чтобы избежать ситуации когда distance = 0 в самой центральной точке
                    if (k == y && j == x)
                        continue;

                    // если  точка не попадает в круглое окно, то не работаем с ней
                    double distance = Math.hypot(j-x, k-y);
                    if (distance > radius)
                        continue;

                    // проверка на выход за границы изображения  - эти точки считаем пустыми
                    if (j<0 || j >= w || k<0 || k>=h) {
                        f_empty += distance;          // чем дальше пискель - тем меньше его вес
                        continue;
                    }

                    if (data[k*w+j] != 0)       //если пиксель закрашен - посчитаем его
                        f_full +=1/distance;
                    else
                        f_empty += 1/distance;
                }
            }

            float fratio = f_full/ f_empty;

            if (fratio > 1)
                fratio = 1/fratio;       // для упрощения (острый/тупой угол)

            pixels.get(i).fratio = fratio;      // понадобится в будущем, при удалении лишних углов
            if (fratio < cornerRatio)
                pixels.get(i).isCorner = true;

        }


        // пробежим по контуру в поисках нескольких угловых точек подряд - оставим из них одну, среднюю.
        // одиночные угловые точки не выкидываем, так как применяется предварительное отсечение по
        // косинусу угла

        // начинать надо с пикселя, следующего за обычным (неугловым), поэтому найдем такой
        int start = 0;
        for (start = 0; start < pixels.size() &&  pixels.get(start).isCorner; start++);   // пустой цикл  - ищем неугловой пиксель

        int n_corners = 0;      // число подряд идущих углов

        for (int i = 1; i <= pixels.size(); i++){       // i=1 потому что начинаем со следующего за пустым пикселем
            int ind = start+i;
            while (ind >= pixels.size())           // закольцуем
                    ind -= pixels.size();


            if (pixels.get(ind).isCorner){      // если пиксель угловой, сразу его отменим, но посчитаем
                pixels.get(ind).isCorner = false;
                n_corners++;
            } else {                            // пиксель неугловой
                if (n_corners == 1){            // если пиксель был один, то вернем его на место - это точно угол
                    int prev = ind - 1;
                    while (prev < 0)
                        prev += pixels.size();

                    pixels.get(prev).isCorner = true;
                }

                if (n_corners > 1) {           // и перед ним были несколько угловых - найдем центральный (они уже все удалены)
                    int corner_index = ind - n_corners/2 - 1;   // индекс реального угла
                    while (corner_index < 0)
                        corner_index += pixels.size();

                    pixels.get(corner_index).isCorner = true;
                }

                n_corners = 0;
            }
        }


        // углы найдены - теперь упростим остальные неугловые точки
        // тупо проредим контур если он большой
        final int minDist = brushSize;
        final int maxDist = 4 * brushSize;

        float min_distance = 0;     // счетчик минимальной дистанции между точками
        float max_distance = 0;     // счетчик максимальной дистанции между точками

        if (pixels.size() > 3) {

            ArrayList<PixelXY> tmpPixels = new ArrayList<>();
            tmpPixels.add(pixels.get(0));

            for (int i = 1; i < pixels.size(); i++) {
                int j = i - 1;            // предыдущая точка

                if (pixels.get(i).isCorner){            // угловые точки всегда добавляем
                    tmpPixels.add(pixels.get(i));
                    min_distance = 0;
                    max_distance = 0;
                } else {
                    int x1 = pixels.get(i).x;
                    int y1 = pixels.get(i).y;
                    int x2 = pixels.get(j).x;
                    int y2 = pixels.get(j).y;
                    float distance = (float)Math.hypot((x1 - x2), (y1 - y2));
                    min_distance += distance;
                    max_distance += distance;

                    if (min_distance >= minDist){       // набрали минимальную дистанцию

                       min_distance = 0;

                       if (max_distance >= maxDist){    // набрали максимальную дистанцию - ставим точку по-любому
                           tmpPixels.add(pixels.get(i));
                           max_distance = 0;
                       } else {                         // если не набрали еще максимальную дистанцию - проверим плоскоту участка
                           // если участок не сильно плоский, то добавим точку, иначе пойдем дальше
                           if (pixels.get(i).cosfi > cornerCos) {
                               tmpPixels.add(pixels.get(i));
                               min_distance = 0;
                               max_distance = 0;
                           }
                       }
                    }
                }
            }
            pixels = tmpPixels;
        }

        // создадим векторный контур, рассчитаем и запишем в него точки
        size = pixels.size();
        for (int i = 0; i < size; i++){

            int x = pixels.get(i).x;
            int y = pixels.get(i).y;

            // если угловая точка
            if (pixels.get(i).isCorner){
//                result.addPoint(new VPoint());
            }
            // предыдущая и последующая точки
            int prev = i-1;
            while(prev < 0)
                prev += size;

            int next = i+1;
            while(next >=size)
                next -= size;

            int x_prev = pixels.get(prev).x;
            int y_prev = pixels.get(prev).y;

            int x_next = pixels.get(next).x;
            int y_next = pixels.get(next).y;


        }

        return result;
    }


    // метод создает и возвращает двумерный массив boolean-ов по размеру img, в котором
    // каждый элемент равен true, если точка является контурной, и false - если нет.
    // на вход принимается обрабатываемое изображение и цвет, который надо выделить контуром
    private static boolean[][] find_contour_points(BufferedImage img, Color color){
        // преобразуем изображение в массив пикселей
        final int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();

        int img_w = img.getWidth();
        int img_h = img.getHeight();
        boolean[][] result = new boolean[img_w][img_h];
        int x,y;
        int nUpperColor, nLeftColor;

        // преобразуем цвет к целочисленному значению
        int nColor = 0;
        nColor += ((color.getAlpha() & 0xFF) << 24); //alpha
        nColor += ((color.getRed() & 0xFF) << 16);   //red
        nColor += ((color.getGreen() & 0xFF) << 8);  //green
        nColor += (color.getBlue() & 0xFF);          //blue

        // пробежим по всем пикселям
        for (int i = 0; i < pixels.length; i++){
            y = i / img_w;
            x = i % img_w;

            // крайние пикселы изображения обрабатываем особо
            if (x == 0 || y == 0 || x == img_w-1 || y == img_h-1) {
                if (pixels[i] == nColor)
                    result[x][y] = true;        // помечаем точку как контурную
                continue;
            }

            //остальные пиксели проверяем специальной трехточечной матрицей
            nUpperColor = pixels[i-img_w];
            nLeftColor = pixels[i-1];

            // если все три цвета одинаковые идем к следующему пикселю
            if (pixels[i] == nColor && nUpperColor == nColor && nLeftColor == nColor)
                continue;

            if (pixels[i] == nColor){
                if (nUpperColor != nColor || nLeftColor != nColor)   // нашли левый или верхний контур
                    result[x][y] = true;        // помечаем точку как контурную
            }
            else {
                if (nUpperColor == nColor)
                    result[x][y-1] = true;        // помечаем точку как контурную

                if (nLeftColor == nColor)
                    result[x-1][y] = true;        // помечаем точку как контурную
            }
        }
        return result;
    }


    // ищет точку контура, следующую за p и возвращает ее. Если не находит,
    // возвращает null. Помечает все просмотренные точки. w, h -  размер карты
    // points - boolean карта контурных точек
    // done_map - уже просмотренные точки
    private static PixelXY find_next_contour_point(PixelXY p, int w, int h, boolean[][]points, boolean[][] done_map) {
        // просматриваем точки, окружающие p, начиная с верхней, по часовой стрелке.
        // для оптимизации контура, сначала просматриваем все "диагональные" соседние точки
        // а потом все остальные. при нахождении точки, помечаем всех соседей как просмотренных
        int x,y;                            // просматриваемая точка

        // ------------диагональные соседи
        // верхняя правая точка
        x = p.x + 1;
        y = p.y - 1;
        if (y >= 0 && x <= w-1){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, w, h, done_map);
                return new PixelXY(x, y);
            }
        }
        // нижняя правая точка
        x = p.x + 1;
        y = p.y + 1;
        if (y <= h-1 && x <= w-1){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, w, h, done_map);
                return new PixelXY(x, y);
            }
        }
        // нижняя левая точка
        x = p.x - 1;
        y = p.y + 1;
        if (y <= h-1 && x >= 0){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, w, h, done_map);
                return new PixelXY(x, y);
            }
        }
        // верхняя левая точка
        x = p.x - 1;
        y = p.y - 1;
        if (y >= 0 && x >= 0){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, w, h, done_map);
                return new PixelXY(x, y);
            }
        }
        // --------------- соседи по горизонтали и вертикали
        // правая точка
        x = p.x + 1;
        y = p.y;
        if (x <= w-1){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, w, h, done_map);
                return new PixelXY(x, y);
            }
        }
        // нижняя точка
        x = p.x;
        y = p.y+1;
        if (y <= h-1){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, w, h, done_map);
                return new PixelXY(x, y);
            }
        }
        // левая точка
        x = p.x-1;
        y = p.y;
        if (x >= 0){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, w, h, done_map);
                return new PixelXY(x, y);
            }
        }
        // верхняя точка
        x = p.x;
        y = p.y-1;
        if (y >= 0){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, w, h, done_map);
                return new PixelXY(x, y);
            }
        }

        make_neighbors_done(p, w, h, done_map);
        return null;
    }


    // помечает соседние с точкой p точки как true. Осуществляет проверку на
    // выход за границы карты. w,h - размер карты. done_map - уже просмотренные точки
    private static void make_neighbors_done(PixelXY p, int w, int h, boolean[][] done_map){
        done_map[p.x][p.y] = true;              // сама точка
        if (p.x > 0)                done_map[p.x-1][p.y] = true;        // слева
        if (p.y > 0)                done_map[p.x][p.y-1] = true;        // сверху
        if (p.x < w-1)              done_map[p.x+1][p.y] = true;        // справа
        if (p.y < h-1)              done_map[p.x][p.y+1] = true;        // снизу

        if (p.x > 0 && p.y > 0)             done_map[p.x-1][p.y-1] = true;      // слева сверху
        if (p.x < w-1 && p.y > 0)           done_map[p.x+1][p.y-1] = true;      // справа сверху
        if (p.x > 0 && p.y < h-1)           done_map[p.x-1][p.y+1] = true;      // слева снизу
        if (p.x < w-1 && p.y < h-1)         done_map[p.x+1][p.y+1] = true;      // справа снизу
    }




}
