package com.dekagames.blot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;


// класс для растрового (временного) контура пятна
class PixelXY{
    public int x;
    public int y;

    public PixelXY(int x, int y){
        this.x = x;
        this.y = y;
    }
}


// класс растрового (временного) контура
class RasterContour {
    public ArrayList<PixelXY> pixels;

    public RasterContour(){
        pixels = new ArrayList<>();
    }

    public void testDraw(BufferedImage img, int color) {
        // преобразуем изображение в массив пикселей
        final int[] data = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int w = img.getWidth();
        int h = img.getHeight();

        for (PixelXY p :pixels){
            data[p.y*w+p.x] = color;
        }
    }

}


public class BrushTool extends Tool {
    public static int  SIZE;                // реально - половина размера (для ускорения)

    protected BufferedImage imgBrush;       // изображение кисти которым будем рисовать
    protected BufferedImage imgTmp;         // временное изображение, которое будем обрабатывать
                                            // оно существует только когда кисть рисует
    protected Graphics2D    grTmp;

    protected Color color;

    private int x0, y0;                     // предыдущие координаты
    private int x, y;                       // текущие координаты кисти

    private int img_h, img_w;               // размеры imgTmp




    public BrushTool(ToolPanel parent, ImageIcon icon, String hint){
        super(parent, icon, hint);

        SIZE = 10;

        imgBrush = new BufferedImage(2 * SIZE, 2 * SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = imgBrush.createGraphics();

        color = new Color(255, 0,0);
        gr.setColor(color);
        gr.fillOval(0,0, 2 * SIZE, 2 * SIZE);
    }


    public void mousePress(MouseEvent e){
        // создадим временное изображение по размеру рисовательной панели, на которое будем рисовать
        DrawPanel dpnl = toolPanel.mainWindow.drawPanel;
        img_h = dpnl.getHeight();
        img_w = dpnl.getWidth();
        imgTmp = new BufferedImage(img_w, img_h, BufferedImage.TYPE_INT_ARGB);
        grTmp = imgTmp.createGraphics();

        x = e.getX();
        y = e.getY();

        x0 = x;
        y0 = y;

        grTmp.drawImage(imgBrush, x - SIZE, y - SIZE,null);
        finish();
    }



    public void mouseRelease(MouseEvent e){
        //  ------------------------- найдем контур пятна ---------------------------------

        // преобразуем изображение в массив пикселей
        final int[] pixels = ((DataBufferInt) imgTmp.getRaster().getDataBuffer()).getData();

        // создадим карту контурных точек в виде boolean-ов
        boolean[][] contour_points = find_contour_points(pixels);

        // найдем все контуры
        ArrayList<RasterContour> contours = get_contours_from_points(contour_points);

        // тест: нарисуем все контуры
        int col = 0xFF0000FF;
        for (RasterContour c:contours){
            c.testDraw(imgTmp, col);
            col = 0xFF000000 | (col * 100);
        }



        finish();
        imgTmp = null;
        grTmp = null;
    }

    // метод создает и возвращает двумерный массив boolean-ов по размеру tmp_img, в котором
    // каждый элемент равен true, если точка является контурной, и false - если нет.
    // на вход принимается одномерный массив полученный из Raster tmpImg.
    private boolean[][] find_contour_points(int[] pixels){
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

            // если все три цвета одинаковые идем к следующему пикселю (!=0 потому что обводим контур)
            if (pixels[i] != 0 && nUpperColor != 0 && nLeftColor != 0)
                continue;

            if (pixels[i] != 0){
                if (nUpperColor == 0 || nLeftColor == 0)   // нашли левый или верхний контур
                    result[x][y] = true;        // помечаем точку как контурную
            }
            else {
                if (nUpperColor != 0)
                    result[x][y-1] = true;        // помечаем точку как контурную

                if (nLeftColor != 0)
                    result[x-1][y] = true;        // помечаем точку как контурную
            }
        }
        return result;
    }



    // метод принимает на вход двумерный массив boolean-ов с обозначенными контурными точками
    // и выделяет из него связанные контуры, состоящие из упорядоченных растровых точек,
    // возвращая их в виде ArrayList-а
    private ArrayList<RasterContour> get_contours_from_points(boolean[][] points){
        ArrayList<RasterContour> result = new ArrayList<>();

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
                            p = find_next_contour_point(p, points, done_map);
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


    // ищет точку контура, следующую за p и возвращает ее. Если не находит,
    // возвращает null. Помечает все просмотренные точки
    private PixelXY find_next_contour_point(PixelXY p, boolean[][]points, boolean[][] done_map) {
        // просматриваем точки, окружающие p, начиная с верхней, по часовой стрелке.
        // для оптимизации контура, сначала просматриваем все "диагональные" соседние точки
        // а потом все остальные. при нахождении точки, помечаем всех соседей как просмотренных
        int x,y;                            // просматриваемая точка

        // ------------диагональные соседи
        // верхняя правая точка
        x = p.x + 1;
        y = p.y - 1;
        if (y >= 0 && x <= img_w-1){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, done_map);
                return new PixelXY(x, y);
            }
        }
        // нижняя правая точка
        x = p.x + 1;
        y = p.y + 1;
        if (y <= img_h-1 && x <= img_w-1){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, done_map);
                return new PixelXY(x, y);
            }
        }
        // нижняя левая точка
        x = p.x - 1;
        y = p.y + 1;
        if (y <= img_h-1 && x >= 0){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, done_map);
                return new PixelXY(x, y);
            }
        }
        // верхняя левая точка
        x = p.x - 1;
        y = p.y - 1;
        if (y >= 0 && x >= 0){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, done_map);
                return new PixelXY(x, y);
            }
        }
        // --------------- соседи по горизонтали и вертикали
        // правая точка
        x = p.x + 1;
        y = p.y;
        if (x <= img_w-1){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, done_map);
                return new PixelXY(x, y);
            }
        }
        // нижняя точка
        x = p.x;
        y = p.y+1;
        if (y <= img_h-1){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, done_map);
                return new PixelXY(x, y);
            }
        }
        // левая точка
        x = p.x-1;
        y = p.y;
        if (x >= 0){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, done_map);
                return new PixelXY(x, y);
            }
        }
        // верхняя точка
        x = p.x;
        y = p.y-1;
        if (y >= 0){
            if(!done_map[x][y] && points[x][y]) {
                make_neighbors_done(p, done_map);
                return new PixelXY(x, y);
            }
        }

        make_neighbors_done(p, done_map);
        return null;
    }


    // помечает соседние с точкой p точки как true. Осуществляет проверку на
    // выход за границы карты.
    private void make_neighbors_done(PixelXY p, boolean[][] done_map){
        done_map[p.x][p.y] = true;              // сама точка
        if (p.x > 0)                done_map[p.x-1][p.y] = true;        // слева
        if (p.y > 0)                done_map[p.x][p.y-1] = true;        // сверху
        if (p.x < img_w-1)          done_map[p.x+1][p.y] = true;        // справа
        if (p.y < img_h-1)          done_map[p.x][p.y+1] = true;        // снизу

        if (p.x > 0 && p.y > 0)             done_map[p.x-1][p.y-1] = true;      // слева сверху
        if (p.x < img_w-1 && p.y > 0)       done_map[p.x+1][p.y-1] = true;      // справа сверху
        if (p.x > 0 && p.y < img_h-1)       done_map[p.x-1][p.y+1] = true;      // слева снизу
        if (p.x < img_w-1 && p.y < img_h-1) done_map[p.x+1][p.y+1] = true;      // справа снизу
    }



    public void mouseDragged(MouseEvent e){
        if (grTmp == null)
            return;

        x = e.getX();
        y = e.getY();

        // рисуем кисть в текущем положении
        grTmp.drawImage(imgBrush, x - SIZE, y - SIZE, null);

        //рассчитаем координаты полигона между предыдущей и нынешней точкой
        double distance = Math.hypot(x-x0, y-y0);
        double cosa = (y-y0) / distance;
        double sina = (x-x0) / distance;
        int rcosa = (int)Math.round(SIZE * cosa);
        int rsina = (int)Math.round(SIZE * sina);

        int[] xpoints = {x0-rcosa, x0+rcosa, x+rcosa, x-rcosa};
        int[] ypoints = {y0+rsina, y0-rsina, y-rsina, y+rsina};

        // рисуем полигон по посчитанным четырем точкам
        grTmp.setColor(color);
        grTmp.fillPolygon(xpoints, ypoints, 4);

        // перепишем координаты
        x0 = x;
        y0 = y;

        finish();
    }

    public void finish(){
        Picture.getInstance().getGraphics().drawImage(imgTmp, 0,0, null);
        toolPanel.mainWindow.drawPanel.repaint();
    }
}
