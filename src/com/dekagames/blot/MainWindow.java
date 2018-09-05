package com.dekagames.blot;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Properties;

public class MainWindow extends JFrame implements ActionListener {

    public static String OPEN_PROJECT_PATH;
    public static String SAVE_PROJECT_PATH;

    public static Properties properties;

    // список недавно открытых файлов
    private String recent1, recent2, recent3;


    // переменная для записи идентификатора кости при сохранении проекта
    private int id_counter;

    // сохранен ли файл
    public boolean is_saved;

    public BlotApp          app;
    public String           strPath;            // полный путь к файлу проекта, если null то еще не сохраняли

    // окно состоит из SplitPane помещенного в Center BorderLayout
    // в левой части SplitPane находится панель с запчастями
    // в правой части - TabPane из двух вкладок: на первой вкладке - редактор модели
    // на второй вкладке - SplitPane c редактором анимации в левой части и списком кадров и
    // действий в правой

    private JToolBar toolbarMain;

    private     JButton     btnNew, btnOpen, btnSave;              // кнопки основного тулбара
    // пункты меню
    private     JMenuItem   mnuFileNew, mnuFileOpen, mnuFileSave, mnuFileSaveAs, mnuFileExit;
    private     JMenuItem   mnuExport;

    private JSplitPane  leftSplit, rightSplit;
    private JPanel      rightPanel, bottomPanel;
    private JToolBar    bonesToolbar, framesToolbar;
    private JButton     btnAddBone, btnDelBone, btnAddFrame, btnDelFrame;

    public  DrawPanelScroll drawPanelScroll;
    public  DrawPanel       drawPanel;

    private JSpinner        spinBoneLayer;
    private JCheckBox       checkVisible;

    public ToolPanel        toolPanel;

//    public  JList                   framesList;         // список кадров
//    public  DefaultListModel<Frame> frameListModel;


    public Picture picture;

    // прочие элементы
    private     JFileChooser    projectFileChooser, boneFileChooser;


    public MainWindow(BlotApp app){
        super("Blot -[Untitled]");
        this.app = app;

        picture = Picture.getInstance();

        properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream("properties.xml"));
            recent1 = properties.getProperty("recent1");
            recent2 = properties.getProperty("recent2");
            recent3 = properties.getProperty("recent3");
        } catch (IOException e) {
            System.out.println("Could not find 'properties.xml'");
        }

        is_saved = true;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // сделаем свой выход с запросом сохранения если не сохранено
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                cmdFileExit();
            }
        });


        // приходится раскрывать окно на весь экран из-за глюка в линуксе, при котором если максимизировать окно после запуска
        // то координаты мыши выдаются со смещением. Поэтому окно должно быть в левом верхнем углу экрана изначально. Также
        // нельзя менять размер окна, пользуясь левой или верхней грницей, можно только справа и снизу. Можно двигать окно -
        // в этом случае все отрабатывает правильно.
//        setExtendedState(JFrame.MAXIMIZED_BOTH);

        initControls();

        setSize(1200, 600);
        setLocation(0,0);
    }

    private void initControls() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");

        mnuFileNew = new JMenuItem("New model");     mnuFileNew.addActionListener(this);     menuFile.add(mnuFileNew);
        mnuFileOpen = new JMenuItem("Open model");   mnuFileOpen.addActionListener(this);    menuFile.add(mnuFileOpen);
        mnuFileSave = new JMenuItem("Save model");   mnuFileSave.addActionListener(this);    menuFile.add(mnuFileSave);
        mnuFileSaveAs = new JMenuItem("Save as..."); mnuFileSaveAs.addActionListener(this);  menuFile.add(mnuFileSaveAs);
        if (recent1 != null){
            menuFile.add(new JSeparator());
            if (recent1 != null) {
                JMenuItem r1 = new JMenuItem(recent1);
                r1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cmdMnuRecentFileOpen(recent1);
                    }
                });
                menuFile.add(r1);
            }
            if (recent2 != null) {
                JMenuItem r2 = new JMenuItem(recent2);
                r2.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cmdMnuRecentFileOpen(recent2);
                    }
                });
                menuFile.add(r2);
            }
            if (recent3 != null) {
                JMenuItem r3 = new JMenuItem(recent3);
                r3.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cmdMnuRecentFileOpen(recent3);
                    }
                });
                menuFile.add(r3);
            }
        }

        menuFile.add(new JSeparator());
        mnuExport = new JMenuItem("Export model..."); mnuExport.addActionListener(this);     menuFile.add(mnuExport);
        menuFile.add(new JSeparator());
        mnuFileExit = new JMenuItem("Exit");         mnuFileExit.addActionListener(this);    menuFile.add(mnuFileExit);

        JMenu menuHelp = new JMenu("Help");
        menuBar.add(menuFile);
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);

        //---------------------------------------------------------------------------------------------------------------
        //          ОСНОВНОЙ ТУЛБАР
        //---------------------------------------------------------------------------------------------------------------
        toolbarMain = new JToolBar();
        btnNew= new JButton(new ImageIcon(getClass().getResource("media/new_file.png")));
        btnNew.setToolTipText("Create new model");          btnNew.addActionListener(this);       toolbarMain.add(btnNew);
        btnOpen = new JButton(new ImageIcon(getClass().getResource("media/open_file.png")));
        btnOpen.setToolTipText("Open model");               btnOpen.addActionListener(this);      toolbarMain.add(btnOpen);
        btnSave = new JButton(new ImageIcon(getClass().getResource("media/save_file.png")));
        btnSave.setToolTipText("Save model");               btnSave.addActionListener(this);      toolbarMain.add(btnSave);

        add(toolbarMain, BorderLayout.NORTH);

        //-----------------------------------------------------------------------------------------------------------------
        // левая панель с причиндаллями
        //-----------------------------------------------------------------------------------------------------------------
        toolPanel = new ToolPanel(this);
//        JPanel bottomPanel = new JPanel(new BorderLayout());
//
//        // создадим дерево костей
//        bonesTree = new JTree(new DefaultMutableTreeNode(skeleton.rootBone));
//        bonesTreeModel = (DefaultTreeModel)bonesTree.getModel();
//
//        bonesTree.setDragEnabled(true);
//        bonesTree.setTransferHandler(new BonesTreeTransferHandler(this));
//        bonesTree.setDropMode(DropMode.ON_OR_INSERT);
//        bonesTree.addTreeSelectionListener(new TreeSelectionListener() {
//            @Override
//            public void valueChanged(TreeSelectionEvent e) {
//                DefaultMutableTreeNode node = (DefaultMutableTreeNode)bonesTree.getLastSelectedPathComponent();
//                if (node == bonesTreeModel.getRoot() || node == null)
//                    drawBonePanel.setCurrentBone(null);
//                else {
//                    Bone b = (Bone)node.getUserObject();
//                    drawBonePanel.setCurrentBone(b);
//                    if (b != null){
//                        checkVisible.setSelected(b.visible);
//                        spinBoneLayer.setValue(b.layer);
//                    }
//                }
//                drawBonePanel.repaint();
//            }
//        });


//        topPanel.add(new JScrollPane(bonesTree), BorderLayout.CENTER);
//
//        drawBonePanel = new DrawBonePanel(this);
//        drawBoneToolbar = new JToolBar();
//        spinBoneLayer = new JSpinner(new SpinnerNumberModel(0,-50,50,1));
//        spinBoneLayer.addChangeListener(new ChangeListener() {
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                Bone b = drawBonePanel.getCurrentBone();
//                if (b != null){
//                    b.layer = (Integer)spinBoneLayer.getValue();
//                    skeleton.sortBones();
//                    drawPanel.repaint();
//                }
//
//            }
//        });
//
//        drawBoneToolbar.add(new JLabel("layer: "));
//        drawBoneToolbar.add(spinBoneLayer);
//        drawBoneToolbar.add(new JSeparator(JSeparator.VERTICAL));
//
//        // чекбокс видимости кости
//        checkVisible = new JCheckBox("visible", true);
//        checkVisible.addChangeListener(new ChangeListener() {
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                Bone b = drawBonePanel.getCurrentBone();
//                if (b != null){
//                    b.visible = checkVisible.isSelected();
//                    drawPanel.repaint();
//                }
//            }
//        });
//
//        drawBoneToolbar.add(checkVisible);
//
//        bottomPanel.add(drawBonePanel, BorderLayout.CENTER);
//        bottomPanel.add(drawBoneToolbar, BorderLayout.NORTH);
//
//        bonesSplit.setTopComponent(topPanel);
//        bonesSplit.setBottomComponent(bottomPanel);
//
//        leftPanel.add(bonesSplit, BorderLayout.CENTER);
//
//
        // правая панель с причиндаллями
        rightPanel = new JPanel(new BorderLayout());

        // создадим список кадров
//        frameListModel = new DefaultListModel<Frame>();
//        framesList = new JList(frameListModel);
//        framesList.addListSelectionListener(new ListSelectionListener() {
//            @Override
//            public void valueChanged(ListSelectionEvent e) {
//                drawPanel.repaint();
//            }
//        });
//        // сразу добавим первый кадр
//        frameListModel.addElement(new Frame(skeleton));
//
//
//
//        rightPanel.add(new JScrollPane(framesList), BorderLayout.CENTER);
//
//        framesToolbar = new JToolBar();
//
//        btnAddFrame = new JButton(new ImageIcon(getClass().getResource("media/add_frame.png")));
//        btnAddFrame.setToolTipText("Add frame");
//        btnAddFrame.addActionListener(this);
//        framesToolbar.add(btnAddFrame);
//
//        btnDelFrame = new JButton(new ImageIcon(getClass().getResource("media/delete_frame.png")));
//        btnDelFrame.setToolTipText("Delete frame");
//        btnDelFrame.addActionListener(this);
//        framesToolbar.add(btnDelFrame);
//
//        rightPanel.add(framesToolbar, BorderLayout.NORTH);
//
//


        // центральная рисовательная панель
        drawPanel = new DrawPanel(this);
        drawPanel.setPreferredSize(new Dimension(1000, 1000));
        drawPanel.setEventListener(toolPanel);          // toolPanel слушает события мыши в drawPanel

        // скролл DrawPanel
        drawPanelScroll = new DrawPanelScroll(drawPanel);

        // правый сплит (панель рисования и панель слоев)
        rightSplit = new JSplitPane();
        rightSplit.setLeftComponent(drawPanelScroll);
        rightSplit.setRightComponent(rightPanel);
        rightSplit.setResizeWeight(0.8);

        // левый сплит - панель с кисточками и правый сплит
        leftSplit = new JSplitPane();
        leftSplit.setResizeWeight(0);
        leftSplit.setLeftComponent(toolPanel);
        leftSplit.setRightComponent(rightSplit);
        leftSplit.setBorder(BorderFactory.createEtchedBorder());


        add(leftSplit, BorderLayout.CENTER);


        // прочие элементы
        projectFileChooser = new JFileChooser();
        projectFileChooser.setFileFilter(new FileNameExtensionFilter("Skeleton editor project", "skprj"));

        boneFileChooser = new JFileChooser();
        boneFileChooser.setMultiSelectionEnabled(true);
        boneFileChooser.setFileFilter(new FileNameExtensionFilter("PNG Image", "png"));

    }


    // рекурсивная процедура добавления костей из дерева в SlonNode для сохранения проекта
//    private void add_bone_to_slon(SlonNode parent_node, Bone bone){
//        // присвоим кости идентификатор
//        bone.idBone = id_counter++;
//
//        SlonNode boneNode = new SlonNode();
//        // запишем параметры этой кости
//        boneNode.setKeyValue("id", bone.idBone);
//        boneNode.setKeyValue("path", bone.getPath());
//        boneNode.setKeyValue("pivotx", bone.pivot.x);
//        boneNode.setKeyValue("pivoty", bone.pivot.y);
//        boneNode.setKeyValue("layer", bone.layer);
//        boneNode.setKeyValue("visible", bone.visible);
//        parent_node.addChild(boneNode);
//        // рекурсия по всем детям
//        for(Bone b:bone.children)
//            add_bone_to_slon(boneNode, b);
//    }

    // сохранение проекта в Slon файл.Вызывается при обычном сохранении и при Save As
//    private void save_project(String full_path){
//        //пересортируем кости на всякий пожарный
//        skeleton.sortBones();
//
//        // создадим слон файл
//        Slon slon = new Slon();
//        SlonNode rootNode = new SlonNode();
//        // запишем дерево костей
//        SlonNode bonesNode = new SlonNode();
//        bonesNode.setKeyValue("name", "bonesNode");
//
//        id_counter = 0;         // для записи идентификатора костей
//        for (Bone b: skeleton.rootBone.children)
//            add_bone_to_slon(bonesNode, b);
//        rootNode.addChild(bonesNode);
//
//        // запишем кадры
//        Frame frame;
//        SlonNode framesNode = new SlonNode();
//        framesNode.setKeyValue("name", "framesNode");
//        for (int i=0; i<frameListModel.size(); i++){
//            frame = frameListModel.get(i);
//            SlonNode frameNode = new SlonNode();
//            // цикл по отсортированным костям
//            for (int j=0; j<skeleton.sortedBones.size(); j++){
//                SlonNode boneNode = new SlonNode();                           // создаем новую запись для кости
//                Bone b = skeleton.sortedBones.get(j);                         // берем кость
//                Position pos = frame.bonesPosition.get(b);                    // выбираем из кадра ее положение
//                boneNode.setKeyValue("id", b.idBone);                         // пишем все характеристики кости
//                boneNode.setKeyValue("x", pos.globX);
//                boneNode.setKeyValue("y", pos.globY);
//                boneNode.setKeyValue("a", pos.globA);
//                // добавим запись кости в запись кадра
//                frameNode.addChild(boneNode);
//            }
//            // добавим запись кадра в запись всех кадров
//            framesNode.addChild(frameNode);
//        }
//        rootNode.addChild(framesNode);
//
//        // запишем весь слон файл
//        slon.setRoot(rootNode);
//        slon.save(full_path);
//        strPath = full_path;
//        setTitle("Skeled-["+ full_path+"]");
//        is_saved = true;
//    }


    // рекурсивная процедура используемая при чтении файла проекта - добавляет кости в дерево из SlonNode
//    private void add_bone_from_slon(Bone parent_bone, SlonNode boneNode){
//        String path = boneNode.getKeyValue("path");
//
//        Bone bone = new Bone(path, skeleton);
//        bone.idBone = boneNode.getKeyAsInt("id");
//        bone.pivot.x = boneNode.getKeyAsFloat("pivotx");
//        bone.pivot.y = boneNode.getKeyAsFloat("pivoty");
//        bone.layer = boneNode.getKeyAsInt("layer");
//        bone.visible = boneNode.getKeyAsBoolean("visible");
//        parent_bone.addChild(bone);
//
//        for (int i=0; i<boneNode.getChildCount(); i++){
//            SlonNode child = boneNode.getChildAt(i);
//            add_bone_from_slon(bone, child);
//        }
//
//
//
//
//    }

    // открывает проект, перезаписывает текущий скелет, возвращает true если успешно false otherwise
//    private boolean open_project(String full_path){
//        Slon slon = new Slon();
//        try {
//            slon.load(full_path);
//        } catch (SlonException e) {
//            e.printStackTrace();
//            return false;
//        }
//
//        // загрузим кости
//        SlonNode bonesNode = slon.getRoot().getChildWithKeyValue("name", "bonesNode");
//        skeleton.reset();// = new Skeleton(this);
//        for (int i=0; i<bonesNode.getChildCount(); i++){
//            SlonNode boneNode = bonesNode.getChildAt(i);
//            add_bone_from_slon(skeleton.rootBone, boneNode);
//        }
//        skeleton.sortBones();
//
//        // загрузим кадры
//        SlonNode framesNode = slon.getRoot().getChildWithKeyValue("name", "framesNode");
//        frameListModel.clear();
//        // цикл по кадрам
//        for (int i=0; i< framesNode.getChildCount(); i++) {
//            SlonNode frameNode = framesNode.getChildAt(i);
//            Frame frame = new Frame(skeleton);
//
//            // цикл по костям отдельного кадра
//            for (int j=0; j<frameNode.getChildCount(); j++) {
//                SlonNode boneNode = frameNode.getChildAt(j);
//                int boneId = boneNode.getKeyAsInt("id");
//                // найдем кость с таким id
//                Bone bone = null;
//                for (Bone b:skeleton.sortedBones){
//                    if (b.idBone == boneId) {
//                        bone = b;
//                        break;
//                    }
//                }
//                // кость найдена
//                if (bone != null){
//                    float x = boneNode.getKeyAsFloat("x");
//                    float y = boneNode.getKeyAsFloat("y");
//                    float a = boneNode.getKeyAsFloat("a");
//
//                    frame.bonesPosition.put(bone, new Position(x, y, a));
//                }
//            }
//
//            frameListModel.addElement(frame);
//        }
//        updateBonesTree();
//        is_saved = true;
//        strPath = full_path;
//        setTitle("Skeled-["+ full_path+"]");
//        drawPanel.repaint();
//
//        // запишем список недавних файлов
//        recent3 = recent2;
//        recent2 = recent1;
//        recent1 = full_path;
//
//        return true;
//    }

    // menu handlers
    private void cmdFileNew(){
        if (!is_saved){
            int answer = JOptionPane.showOptionDialog(this, "Project not saved! Do you want to save it?", "Skeled",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,null,null);
            if (answer == JOptionPane.YES_OPTION)
                cmdFileSave();
            else if (answer == JOptionPane.CANCEL_OPTION ||answer ==JOptionPane.CLOSED_OPTION)
                return;
        }

        strPath = null;
        is_saved = true;
//
//        // новый список кадров
//        frameListModel.clear();
//        // сразу добавим первый кадр
//        frameListModel.addElement(new Frame(skeleton));
//
//        skeleton.reset();
        setTitle("Blot - [Untitled]");

//        updateBonesTree();
        drawPanel.repaint();
    }

    private void cmdFileOpen(){
//        if (!is_saved){
//            int answer = JOptionPane.showOptionDialog(this, "Project not saved! Do you want to save it?", "Skeled",
//                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,null,null);
//            if (answer == JOptionPane.YES_OPTION)
//                cmdFileSave();
//            else if (answer == JOptionPane.CANCEL_OPTION ||answer ==JOptionPane.CLOSED_OPTION)
//                return;
//        }
//
//        int retVal = projectFileChooser.showOpenDialog(this);
//        if (retVal == JFileChooser.APPROVE_OPTION) {
//            File file = projectFileChooser.getSelectedFile();
//            String  filePath = file.getAbsolutePath();
//
//            boolean res = open_project(filePath);
//            if (!res){
//                JOptionPane.showMessageDialog(this, "Could not opened file "+filePath+"!");
//            }
//        }
    }


    private void cmdMnuRecentFileOpen(String filename){
//        if (!is_saved){
//            int answer = JOptionPane.showOptionDialog(this, "Project not saved! Do you want to save it?", "Skeled",
//                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,null,null);
//            if (answer == JOptionPane.YES_OPTION)
//                cmdFileSave();
//            else if (answer == JOptionPane.CANCEL_OPTION ||answer ==JOptionPane.CLOSED_OPTION)
//                return;
//        }
//
//        boolean res = open_project(filename);
//        if (!res){
//            JOptionPane.showMessageDialog(this, "Could not opened file "+filename+"!");
//        }
    }


    private void cmdFileSave(){
//        // если еще ни разу не сохраняли файл, вызовем команду Save as
//        if (strPath == null)
//            cmdFileSaveAs();
//        else
//            save_project(strPath);
    }

    private void cmdFileSaveAs(){
//        int returnVal = projectFileChooser.showSaveDialog(this);
//        if (returnVal == JFileChooser.APPROVE_OPTION) {
//            File file = projectFileChooser.getSelectedFile();
//            if (file.exists()){
//                int confirm = JOptionPane.showConfirmDialog(this, "Owerwrite file "+file.getName()+"?", "SkelEd", JOptionPane.YES_NO_OPTION);
//                if (confirm == JOptionPane.NO_OPTION) return;
//            }
//            String  filePath = file.getAbsolutePath();
//            String  fileExt = Utils.getFileExtension(filePath);
//            if (fileExt==null || !fileExt.equalsIgnoreCase("skprj")) filePath += ".skprj";
//
//            save_project(filePath);
//        }
    }

    private void cmdExport(){
//        new ExportDialog(this).setVisible(true);
    }

    // выход с запросом сохранения
    private void cmdFileExit(){
        if (!is_saved){
            int answer = JOptionPane.showOptionDialog(this, "Project not saved! Do you want to save it?", "Skeled",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,null,null);
            if (answer == JOptionPane.YES_OPTION)
                cmdFileSave();
            else if (answer == JOptionPane.CANCEL_OPTION ||answer ==JOptionPane.CLOSED_OPTION)
                return;
        }
        try {
            if (recent1 != null) properties.setProperty("recent1", recent1);
            if (recent2 != null) properties.setProperty("recent2", recent2);
            if (recent3 != null) properties.setProperty("recent3", recent3);

            properties.storeToXML(new FileOutputStream("properties.xml"), "SkelEd properties file");
        } catch (FileNotFoundException e) {
            System.out.println("Could not find file 'properties.xml'");
        } catch (IOException e) {
            System.out.println("IOExeption while saving properties");
            e.printStackTrace();
        }
        System.exit(0);
    }


    // нажата кнопка добавления кадра
    private void cmdAddFrame(){
//        // получим выделенный кадр из списка. Если кадр не выделен, будем копировать первый кадр по умолчанию
//        int ind = framesList.getSelectedIndex();
//        if (ind < 0) ind = 0;
//        Frame fr = frameListModel.get(ind);
//
//        // вставим копию кадра после выделенного
//        try {
//            frameListModel.add(ind + 1, new Frame(fr));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        is_saved = false;
    }


    // нажата кнопка удаления кадра
    private void cmdDelFrame(){
//        // если кадр всего один - не удаляем его
//        if (frameListModel.size() <= 1) return;
//
//        // получим выделенный кадр из списка.
//        int ind = framesList.getSelectedIndex();
//        if (ind < 0) {
//            JOptionPane.showMessageDialog(this, "Select the frame to delete, asshole!");
//            return;
//        }
//
//        frameListModel.remove(ind);
//        is_saved = false;
    }



    // нажата кнопочка добавления нового спрайта
    private void cmdAddBone(){
//        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)bonesTree.getLastSelectedPathComponent();
//        if (selectedNode == null)
//            selectedNode = (DefaultMutableTreeNode)bonesTreeModel.getRoot();
//        Bone selectedBone = (Bone)selectedNode.getUserObject();
//
//        int returnVal = boneFileChooser.showOpenDialog(this);
//        if (returnVal == JFileChooser.APPROVE_OPTION) {
//            File[] files = boneFileChooser.getSelectedFiles();
//            for (int i =0; i<files.length; i++){
//                File f = files[i];
//                Bone bone = new Bone(f.getAbsolutePath(), skeleton);
//                selectedBone.addChild(bone);
//
//                // добавим новую кость во все уже существующие кадры
//                for (int j=0; j<frameListModel.size(); j++)
//                    frameListModel.get(j).bonesPosition.put(bone, new Position(0,0,0));
//
//                skeleton.sortBones();
//            }
//            updateBonesTree();
//            drawPanel.repaint();
//            is_saved = false;
//        }
    }

    // нажата кнопочка удаления запчасти
    private void cmdDelBone(){
//        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)bonesTree.getLastSelectedPathComponent();
//
//        if (selectedNode == null) {
//            JOptionPane.showMessageDialog(this, "Select the bone before!");
//            return;
//        }
//        else {
//            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this, "Are you sure to delete bone from model?", "SkelEd",
//                    JOptionPane.YES_NO_OPTION)) return;
//
//            else {
//                Bone b = (Bone)selectedNode.getUserObject();
//                Bone p = b.parent;
//                p.removeChild(b);
//
//                // уберем кость из каждого кадра
//                for (int i=0; i<frameListModel.size(); i++)
//                    frameListModel.get(i).bonesPosition.remove(b);
//
//                skeleton.sortBones();
//                updateBonesTree();
//                drawPanel.repaint();
//                is_saved = false;
//            }
//        }
    }


    // диспетчеризация событий от меню и тулбаров
    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == btnNew || obj == mnuFileNew) cmdFileNew();
        if (obj == btnOpen || obj == mnuFileOpen) cmdFileOpen();
        if (obj == btnSave || obj == mnuFileSave) cmdFileSave();
//        if (obj == btnPack || obj == mnuAtlasPack) cmdPackAtlas();
        if (obj == mnuFileSaveAs) cmdFileSaveAs();
        if (obj == mnuExport)     cmdExport();
        if (obj == mnuFileExit) cmdFileExit();
        // команды левого тулбара
        if (obj == btnAddBone)  cmdAddBone();
        if (obj == btnDelBone)  cmdDelBone();
        // команды тулбара кадров
        if (obj == btnAddFrame)    cmdAddFrame();
        if (obj == btnDelFrame)    cmdDelFrame();
    }

    // рекурсия используется
//    private void update_node(DefaultMutableTreeNode node) {
//        Bone bone = (Bone)node.getUserObject();
//        for (int i=0; i<bone.children.size(); i++){
//            DefaultMutableTreeNode n = new DefaultMutableTreeNode(bone.children.get(i));
//            node.add(n);
//            update_node(n);
//        }
//    }

    // приводим в соответствие дерево костей на экране к дереву в памяти
//    public void updateBonesTree(){
//        Bone b = skeleton.rootBone;     // начальная инициализация
//        DefaultMutableTreeNode n = new DefaultMutableTreeNode(b);
//        bonesTreeModel.setRoot(n);
//        update_node(n);
//        bonesTreeModel.reload();
//        bonesTree.repaint();
//    }

    // экспортитуем полученный атлас в файл
    private void export_atlas(String path){
//        Slon slon = new Slon();
//        SlonNode rootNode = new SlonNode();
//        SlonNode spritesNode = new SlonNode();
//        spritesNode.setKeyValue("name", "sprites");
//
//        SlonNode fontsNode = new SlonNode();
//        fontsNode.setKeyValue("name","fonts");
//
//        rootNode.setKeyValue("width", (String)comboWidth.getSelectedItem());
//        rootNode.setKeyValue("height", (String)comboHeight.getSelectedItem());
//
//        // экспорт спрайтов в секцию со спрайтами
//        for (int i=0; i< spriteTree.getCount(); i++){
//            SlonNode spriteSlon = new SlonNode();
//            SpriteNode sprNode = spriteTree.getNodeAt(i);
//            spriteSlon.setKeyValue("name", sprNode.toString());
//            for (int j=0; j<sprNode.getChildCount(); j++){
//                SlonNode frameSlon = new SlonNode();
//                FrameNode frmNode = sprNode.getChildAt(j);
//                // запишем характеристики отдельного кадра
//                frameSlon.setKeyValue("x", (frmNode.pictureRect.x + sprNode.border));
//                frameSlon.setKeyValue("y", (frmNode.pictureRect.y + sprNode.border));
//                frameSlon.setKeyValue("w", (frmNode.pictureRect.w - 2 * sprNode.border));
//                frameSlon.setKeyValue("h", (frmNode.pictureRect.h - 2 * sprNode.border));
//                frameSlon.setKeyValue("pivotX", (frmNode.xPivot - frmNode.cropLeft));
//                frameSlon.setKeyValue("pivotY", (frmNode.yPivot - frmNode.cropTop));
//                spriteSlon.addChild(frameSlon);
//            }
//            spritesNode.addChild(spriteSlon);
//        }
//        rootNode.addChild(spritesNode);
//
//        // экспорт шрифтов в секцию со шрифтами
//        for (int i=0; i< fontTree.getCount(); i++){
//            SlonNode fontSlon = new SlonNode();
//            FontNode fntNode = fontTree.getNodeAt(i);
//            fontSlon.setKeyValue("name", fntNode.toString());
//            // выгрузим каждую буковку
//            for (int j=0; j<fntNode.getChildCount(); j++){
//                SlonNode glyphSlon = new SlonNode();
//                GlyphNode glphNode = fntNode.getChildAt(j);
//                glyphSlon.setKeyValue("glyph",glphNode.glyph);
//                glyphSlon.setKeyValue("x", (glphNode.pictureRect.x + fntNode.border));
//                glyphSlon.setKeyValue("y", (glphNode.pictureRect.y + fntNode.border));
//                glyphSlon.setKeyValue("w", (glphNode.pictureRect.w - 2 * fntNode.border));
//                glyphSlon.setKeyValue("h", (glphNode.pictureRect.h - 2 * fntNode.border));
//
//                // расстояние от origin до левой границы изображения буквы. Может быть отрицательным.
//                glyphSlon.setKeyValue("lsb", (glphNode.glyphMetrics.getLSB()+fntNode.border));
//                // расстояние от верха строки до origin символа
//                glyphSlon.setKeyValue("originY", (-glphNode.glyphMetrics.getBounds2D().getBounds().y + fntNode.border));
//                // расстояние от origin до origin следующего символа
//                glyphSlon.setKeyValue("advance", glphNode.glyphMetrics.getAdvance());
//                fontSlon.addChild(glyphSlon);
//            }
//            fontsNode.addChild(fontSlon);
//        }
//        rootNode.addChild(fontsNode);
//
//
//        slon.setRoot(rootNode);
//
////	    slon.load("C:\\test.slon");
//        slon.save(path);
    }


}
