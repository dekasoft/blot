package com.dekagames.blot.algorithm;

import com.dekagames.blot.Picture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;

/**
 * класс растрового контура. используется как промежуточное звено при переходе от
 * растрового пятна к векторному контуру
 */
public class RasterContour {
    private ArrayList<RasterPoint> points;

    private RasterContour(){
        points = new ArrayList<>();
    }

    public void testDraw(BufferedImage img, int color) {
        // преобразуем изображение в массив пикселей
        final int[] data = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int w = img.getWidth();
        //int h = img.getHeight();

        int c1 = 0xFF/50;
        int col;
        for (RasterPoint p : points){
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
    public static ArrayList<RasterContour> get_raster_contours(BufferedImage img, Color color){
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
                        RasterPoint p = new RasterPoint(x,y);

                        while (p != null) {
                            cntr.points.add(p);
                            p = find_next_contour_point(p, img_w, img_h, points, done_map);
                        }
                        // невозможно маленькие контуры просто не добавляем
                        if (cntr.points.size() > 4)
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
        for (int i = points.size()-1; i>=0; i-=2){
            points.remove(i);
        }

        // посчитаем косинус угла при каждой точке и найдем уравнение
        // касательной в каждой точке (делаем на этом этапе, так как так
        // получится точнее, чем когда будут выкинуты лишние точки)

        int size = points.size();
        int step = 1+brushSize/3;

        for (int i = 0; i < size; i++){
            // i - предудущий сосед
            // j - точка, для которой будем делать расчет
            // k - последующий сосед
            int j = i + step;
            while (j >= size) j = j - size;   // закольцуем

            int k = j + step;
            while (k >= size) k = k - size;

            RasterPoint currPoint = points.get(j);
            RasterPoint prevPoint = points.get(i);
            RasterPoint nextPoint = points.get(k);

            // косинус угла
            currPoint.cosfi = currPoint.getCos(prevPoint, nextPoint);

            // найдем касательную в точке
            RasterLine line = new RasterLine(prevPoint, nextPoint);
            currPoint.tangent = line.getParallel(currPoint);
        }




        // детектор SUSAN
        int radius = 1+brushSize/2;       // радиус круга для просмотра. Должен зависеть от размера кисти которым рисовалось пятно (?)
        size = points.size();

        // пробежим по всем пикселям контура, будем считать соотношение закрашенных и незакрашенных пикселей
        // в круге с центром в текущем пикселе/
        // предварительно отберем по косинусу
        for (int i = 0; i < size; i++){
            if (points.get(i).cosfi < cornerCos)
                continue;

            int x = points.get(i).x;
            int y = points.get(i).y;

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

            points.get(i).fratio = fratio;      // понадобится в будущем, при удалении лишних углов
            if (fratio < cornerRatio)
                points.get(i).isCorner = true;

        }


        // пробежим по контуру в поисках нескольких угловых точек подряд - оставим из них одну, среднюю.
        // одиночные угловые точки не выкидываем, так как применялось предварительное отсечение по
        // косинусу угла

        // начинать надо с пикселя, следующего за обычным (неугловым), поэтому найдем такой
        int start = 0;
        for (start = 0; start < points.size() &&  points.get(start).isCorner; start++);   // пустой цикл  - ищем неугловой пиксель

        int n_corners = 0;      // число подряд идущих углов

        for (int i = 1; i <= points.size(); i++){       // i=1 потому что начинаем со следующего за пустым пикселем
            int ind = start+i;
            while (ind >= points.size())           // закольцуем
                    ind -= points.size();


            if (points.get(ind).isCorner){      // если пиксель угловой, сразу его отменим, но посчитаем
                points.get(ind).isCorner = false;
                n_corners++;
            } else {                            // пиксель неугловой
                if (n_corners == 1){            // если пиксель был один, то вернем его на место - это точно угол
                                                // (он прошел проверку по косинусу и по susan и при этом один. Точно угол)
                    int prev = ind - 1;
                    while (prev < 0)
                        prev += points.size();

                    points.get(prev).isCorner = true;
                }

                if (n_corners > 1) {           // и перед ним были несколько угловых - найдем центральный (они уже все удалены)
                    int corner_index = ind - n_corners/2 - 1;   // индекс реального угла
                    while (corner_index < 0)
                        corner_index += points.size();

                    points.get(corner_index).isCorner = true;
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

        if (points.size() > 3) {

            ArrayList<RasterPoint> tmpPixels = new ArrayList<>();
            tmpPixels.add(points.get(0));

            for (int i = 1; i < points.size(); i++) {
                int j = i - 1;            // предыдущая точка

                if (points.get(i).isCorner){            // угловые точки всегда добавляем
                    tmpPixels.add(points.get(i));
                    min_distance = 0;
                    max_distance = 0;
                } else {
                    int x1 = points.get(i).x;
                    int y1 = points.get(i).y;
                    int x2 = points.get(j).x;
                    int y2 = points.get(j).y;
                    float distance = (float)Math.hypot((x1 - x2), (y1 - y2));
                    min_distance += distance;
                    max_distance += distance;

                    if (min_distance >= minDist){       // набрали минимальную дистанцию

                       min_distance = 0;

                       if (max_distance >= maxDist){    // набрали максимальную дистанцию - ставим точку по-любому
                           tmpPixels.add(points.get(i));
                           max_distance = 0;
                       } else {                         // если не набрали еще максимальную дистанцию - проверим плоскоту участка
                           // если участок не сильно плоский, то добавим точку, иначе пойдем дальше
                           if (points.get(i).cosfi > cornerCos) {
                               tmpPixels.add(points.get(i));
                               min_distance = 0;
                               max_distance = 0;
                           }
                       }
                    }
                }
            }
            points = tmpPixels;
        }

        // создадим векторный контур, рассчитаем и запишем в него точки
        // векторный контур создается так, что координата (0;0) находится
        // в левом верхнем углу, как у растра. Для перевода в систему координат
        // Picture, необходимо вызвать соответствующий метод
        size = points.size();
        double factor = Picture.getInstance().getPixelSize();   // коэффициент перевода в вектор
        for (int i = 0; i < size; i++){

            RasterPoint currPoint = points.get(i);

            // если угловая точка
            if (currPoint.isCorner){
                result.addPoint(new VPoint(currPoint.x * factor, currPoint.y * factor));
                continue;
            }
            // предыдущая и последующая точки
            int prev = i-1;
            while(prev < 0)
                prev += size;

            int next = i+1;
            while(next >=size)
                next -= size;

            RasterPoint prevPoint = points.get(prev);
            RasterPoint nextPoint = points.get(next);

            // расстояние до предыдущей и последующей точки
            double dist_curr_prev = currPoint.distanceTo(prevPoint.x, prevPoint.y);
            double dist_curr_next = currPoint.distanceTo(nextPoint.x, nextPoint.y);

            // найдем точки p1 и p2 в первом варианте
            RasterCoords p1_1 = currPoint.tangent.getPointOnDistanceFrom(currPoint, -dist_curr_prev/3);
            RasterCoords p2_1 = currPoint.tangent.getPointOnDistanceFrom(currPoint, dist_curr_next/3);

            // найдем точки p1 и p2 во втором варианте
            RasterCoords p1_2 = currPoint.tangent.getPointOnDistanceFrom(currPoint, dist_curr_prev/3);
            RasterCoords p2_2 = currPoint.tangent.getPointOnDistanceFrom(currPoint, -dist_curr_next/3);

            // расстояние от prev до next по первому и по второму варианту
            double l1 = prevPoint.distanceTo(p1_1.x, p1_1.y) + p1_1.distanceTo(p2_1.x, p2_1.y) + p2_1.distanceTo(nextPoint.x, nextPoint.y);
            double l2 = prevPoint.distanceTo(p1_2.x, p1_2.y) + p1_2.distanceTo(p2_2.x, p2_2.y) + p2_2.distanceTo(nextPoint.x, nextPoint.y);

            // выберем нужные точки
            RasterCoords p1, p2;
            if (l1 < l2){       // первый вариант
                p1 = p1_1;
                p2 = p2_1;
            } else {            // второй вариант
                p1 = p1_2;
                p2 = p2_2;
            }

            // добавим точку
            result.addPoint(new VPoint(currPoint.x * factor, currPoint.y * factor,
                                                        p1.x * factor, p1.y * factor,
                                                               p2.x * factor, p2.y * factor));
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
    private static RasterPoint find_next_contour_point(RasterPoint p, int w, int h, boolean[][]points, boolean[][] done_map) {
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
                return new RasterPoint(x, y);
            }
        }
        // нижняя правая точка
        x = p.x + 1;
        y = p.y + 1;
        if (y <= h-1 && x <= w-1){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, w, h, done_map);
                return new RasterPoint(x, y);
            }
        }
        // нижняя левая точка
        x = p.x - 1;
        y = p.y + 1;
        if (y <= h-1 && x >= 0){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, w, h, done_map);
                return new RasterPoint(x, y);
            }
        }
        // верхняя левая точка
        x = p.x - 1;
        y = p.y - 1;
        if (y >= 0 && x >= 0){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, w, h, done_map);
                return new RasterPoint(x, y);
            }
        }
        // --------------- соседи по горизонтали и вертикали
        // правая точка
        x = p.x + 1;
        y = p.y;
        if (x <= w-1){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, w, h, done_map);
                return new RasterPoint(x, y);
            }
        }
        // нижняя точка
        x = p.x;
        y = p.y+1;
        if (y <= h-1){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, w, h, done_map);
                return new RasterPoint(x, y);
            }
        }
        // левая точка
        x = p.x-1;
        y = p.y;
        if (x >= 0){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, w, h, done_map);
                return new RasterPoint(x, y);
            }
        }
        // верхняя точка
        x = p.x;
        y = p.y-1;
        if (y >= 0){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, w, h, done_map);
                return new RasterPoint(x, y);
            }
        }

        make_neighbors_done(p, w, h, done_map);
        return null;
    }


    // помечает соседние с точкой p точки как true. Осуществляет проверку на
    // выход за границы карты. w,h - размер карты. done_map - уже просмотренные точки
    private static void make_neighbors_done(RasterPoint p, int w, int h, boolean[][] done_map){
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
