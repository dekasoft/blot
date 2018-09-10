package com.dekagames.blot.algorithm;

import com.dekagames.blot.spline.Contour;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;

/**
 * класс растрового контура. используется как промежуточное звено при переходе от
 * растрового пятна к векторному контуру
 */
public class RasterContour {
    public ArrayList<PixelXY> pixels;

    public RasterContour(){
        pixels = new ArrayList<>();
    }

    public void testDraw(BufferedImage img, int color) {
        // преобразуем изображение в массив пикселей
        final int[] data = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int w = img.getWidth();
        //int h = img.getHeight();

        int col;
        for (PixelXY p :pixels){
            if (Math.abs(p.delta) > 16)
                col = 0xFF000000;
            else
                col = color;

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

    public Contour toSpline(){
        Contour result = new Contour();

        // пробежимся по контуру - рассчитаем для каждой точки ее delta - отклонение точки от прямой,
        // проведенной через ее соседей. Если у точки delta = 0, это значит что она лежит на одной прямой
        // со своими соседями, т.е. является потенциальным "клиентом" на удаление из контура. Соседи для
        // точки берутся на расстоянии step от нее.

        int step = 1;
        int size = pixels.size();

        for (int i = 0; i < size; i++){
            // i - предудущий сосед
            // j - точка, для которой будем делать расчет
            // k - последующий сосед
            int j = i + step;
            while (j >= size) j = j - size;   // закольцуем

            int k = j + step;
            while (k >= size) k = k - size;

            // отклонение будем рассчитывать по формуле векторного произведения в координатной форме
            // delta = x1*y2 - x2*y1, где (x1,y1) - координаты вектора (ik), (x2,y2) - координаты вектора (i,j)
            // знак delta показывает по какую сторону от отрезка лежит точка. если delta = 0, то все три
            // точки лежат на одной прямой
            int x1 = pixels.get(k).x - pixels.get(i).x;
            int y1 = pixels.get(k).y - pixels.get(i).y;
            int x2 = pixels.get(j).x - pixels.get(i).x;
            int y2 = pixels.get(j).y - pixels.get(i).y;
            pixels.get(j).delta = x1*y2 - x2*y1;

        }

        // ---------------------- test ---------------------------------
        // многоступенчатая оптимизация

        //удалим точки с нулевым delta
        for (int i = pixels.size()-1; i>=0; i--){
            if (pixels.get(i).delta == 0)
                pixels.remove(i);
        }


        step = 1;
        size = pixels.size();

        for (int i = 0; i < size; i++){
            // i - предудущий сосед
            // j - точка, для которой будем делать расчет
            // k - последующий сосед
            int j = i + step;
            while (j >= size) j = j - size;   // закольцуем

            int k = j + step;
            while (k >= size) k = k - size;

            // отклонение будем рассчитывать по формуле векторного произведения в координатной форме
            // delta = x1*y2 - x2*y1, где (x1,y1) - координаты вектора (ik), (x2,y2) - координаты вектора (i,j)
            // знак delta показывает по какую сторону от отрезка лежит точка. если delta = 0, то все три
            // точки лежат на одной прямой
            int x1 = pixels.get(k).x - pixels.get(i).x;
            int y1 = pixels.get(k).y - pixels.get(i).y;
            int x2 = pixels.get(j).x - pixels.get(i).x;
            int y2 = pixels.get(j).y - pixels.get(i).y;
            pixels.get(j).delta = x1*y2 - x2*y1;

        }


//--------------------------- test -------------------------------------- 3 ШАГ ----------------
        //удалим точки с малым delta
        for (int i = pixels.size()-1; i>=0; i--){
            if (Math.abs(pixels.get(i).delta) <= 2)
                pixels.remove(i);
        }


        step = 1;
        size = pixels.size();

        for (int i = 0; i < size; i++){
            // i - предудущий сосед
            // j - точка, для которой будем делать расчет
            // k - последующий сосед
            int j = i + step;
            while (j >= size) j = j - size;   // закольцуем

            int k = j + step;
            while (k >= size) k = k - size;

            // отклонение будем рассчитывать по формуле векторного произведения в координатной форме
            // delta = x1*y2 - x2*y1, где (x1,y1) - координаты вектора (ik), (x2,y2) - координаты вектора (i,j)
            // знак delta показывает по какую сторону от отрезка лежит точка. если delta = 0, то все три
            // точки лежат на одной прямой
            int x1 = pixels.get(k).x - pixels.get(i).x;
            int y1 = pixels.get(k).y - pixels.get(i).y;
            int x2 = pixels.get(j).x - pixels.get(i).x;
            int y2 = pixels.get(j).y - pixels.get(i).y;
            pixels.get(j).delta = x1*y2 - x2*y1;

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