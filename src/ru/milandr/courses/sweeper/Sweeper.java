package ru.milandr.courses.sweeper;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;


public class Sweeper extends Application {

    private Scene scene;

    private Tile[][] grid = new Tile[xTiles][yTiles];

    private static final int TAB_HEIGHT = 50;

    private static int tileSize = 40;
    private static int xTiles = 20;
    private static int yTiles = 16;

    private long allBombs = 0;
    private long openedTiles = 0;
    private long flagsAvailable = 0;

    private Image flag = new Image("/ru/milandr/courses/sweeper/ilya.jpg");
    private Image mine = new Image("/ru/milandr/courses/sweeper/mine.png");
    private Image mineCrossed = new Image("/ru/milandr/courses/sweeper/notamine.png");

    private Text flags = new Text();
    private Text info = new Text();


    private Parent createContent() {
        allBombs = 0;
        openedTiles = 0;
        flagsAvailable = 0;
        int WIDTH = xTiles * tileSize;
        int HEIGHT = yTiles * tileSize;

        Pane root = new Pane();

        MenuItem level1 = new MenuItem("Новичок");
        level1.setOnAction(event -> {
            tileSize = 80;
            xTiles = 10;
            yTiles = 8;
            scene.setRoot(createContent());
        });

        MenuItem level2 = new MenuItem("Бывалый");
        level2.setOnAction(event -> {
            tileSize = 60;
            xTiles = 15;
            yTiles = 12;
            scene.setRoot(createContent());
        });

        MenuItem level3 = new MenuItem("Эксперт");
        level3.setOnAction(event -> {
            tileSize = 40;
            xTiles = 20;
            yTiles = 16;
            scene.setRoot(createContent());
        });

        MenuItem quit = new MenuItem("Quit");
        quit.setOnAction(event -> Platform.exit());

        MenuItem restart = new MenuItem("Restart");
        restart.setOnAction(event -> scene.setRoot(createContent()));


        MenuButton menuButton = new MenuButton("Меню", null, level1, level2, level3, restart, quit);

        menuButton.setTranslateX(100);
        menuButton.setTranslateY(TAB_HEIGHT * 0.2);

        root.setPrefSize(WIDTH, HEIGHT + TAB_HEIGHT);
        Rectangle tab = new Rectangle(WIDTH, TAB_HEIGHT);
        tab.setStroke(Color.DARKGRAY);
        tab.setFill(Color.DARKGREY);
        root.getChildren().addAll(tab, menuButton);


        for (int y = 0; y < yTiles; y++) {
            for (int x = 0; x < xTiles; x++) {
                Tile tile = new Tile(x, y, Math.random() < 0.2);

                if (tile.hasBomb) {
                    allBombs++;
                    tile.imageView = tile.createImage(mine);
                    tile.imageView.setImage(null);
                }

                grid[x][y] = tile;
                root.getChildren().add(grid[x][y]);
            }
        }

        flagsAvailable = allBombs;

        ImageView imageView = new ImageView();
        imageView.setImage(flag);
        imageView.setX(10);
        imageView.setY(TAB_HEIGHT * 0.1);
        imageView.setFitHeight(38);
        imageView.setFitWidth(38);

        flags.setFont(Font.font(18));
        flags.setTranslateX(50);
        flags.setTranslateY(TAB_HEIGHT * 0.6);
        flags.setText(String.format(": %d", flagsAvailable));
        flags.setVisible(true);

        info.setFont(Font.font(30));
        info.setTranslateX(WIDTH * 0.5);
        info.setTranslateY(TAB_HEIGHT * 0.7);
        info.setText("");
        info.setVisible(true);


        root.getChildren().addAll(imageView, flags, info);

        for (int y = 0; y < yTiles; y++) {
            for (int x = 0; x < xTiles; x++) {
                Tile tile = grid[x][y];

                if (tile.hasBomb)
                    continue;

                long bombs = getNeighbors(tile).stream().filter(t -> t.hasBomb).count();
                if (bombs > 0)
                    tile.text.setText(String.format("%d", bombs));
            }
        }

        return root;
    }

    private List<Tile> getNeighbors(Tile tile) {
        List<Tile> neighbors = new ArrayList<>();

        int[] points = new int[]{
                -1, -1,
                -1, 0,
                -1, 1,
                0, -1,
                0, 1,
                1, -1,
                1, 0,
                1, 1
        };
        for (int i = 0; i < points.length; i++) {
            int dx = points[i];
            int dy = points[++i];

            int newX = tile.x + dx;
            int newY = tile.y + dy;

            if (newX >= 0 && newX < xTiles
                    && newY >= 0 && newY < yTiles)
                neighbors.add(grid[newX][newY]);

        }
        return neighbors;
    }

    private class Tile extends StackPane {
        private int x, y;
        private boolean hasBomb;
        private boolean isOpened = false;
        private boolean isFlagged = false;
        private ImageView imageView;

        private Rectangle border = new Rectangle(tileSize - 2, tileSize - 2);


        private Text text = new Text();

        private Tile(int x, int y, boolean hasBomb) {
            this.x = x;
            this.y = y;
            this.hasBomb = hasBomb;

            border.setStroke(Color.LIGHTGREY);
            border.setFill(Color.DARKGREY);

            text.setFont(Font.font(tileSize * 0.5));
            text.setVisible(false);

            getChildren().addAll(border, text);

            setTranslateX(x * tileSize);
            setTranslateY(y * tileSize + TAB_HEIGHT);
            setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.SECONDARY)
                    flag();
                else
                    open();
            });
            //setOnMouseClicked(e -> open());
        }

        private ImageView createImage(Image image) {
            ImageView imageView = new ImageView();
            imageView.setImage(image);
            imageView.setX(1);
            imageView.setY(1);
            imageView.setFitHeight(tileSize - 2);
            imageView.setFitWidth(tileSize - 2);
            getChildren().addAll(imageView);
            return imageView;
        }

        private void showBombs() {
            for (int y = 0; y < yTiles; y++) {
                for (int x = 0; x < xTiles; x++) {
                    Tile tile = grid[x][y];

                    if (tile.hasBomb)
                        tile.imageView = tile.createImage(mine);
                    if (!tile.hasBomb && tile.isFlagged)
                        tile.imageView = tile.createImage(mineCrossed);
                }
            }
        }

        private void stopGame() {
            for (int y = 0; y < yTiles; y++) {
                for (int x = 0; x < xTiles; x++) {
                    Tile tile = grid[x][y];
                    tile.setOnMouseClicked(null);
                }
            }
        }

        private void flag() {
            if (isOpened)
                return;

            if (!isFlagged) {
                imageView = createImage(flag);
                isFlagged = true;
                flagsAvailable--;
                flags.setText(String.format(": %d", flagsAvailable));
            } else {
                imageView.setImage(null);
                isFlagged = false;
                flagsAvailable++;
                flags.setText(String.format(": %d", flagsAvailable));
            }
        }

        private void open() {

            if (openedTiles == 0 && hasBomb) {
                flag();
                setOnMouseClicked(null);
                openedTiles++;
                return;
            }

            if (isOpened || isFlagged)
                return;

            if (hasBomb) {
                border.setFill(Color.RED);
                info.setText("Ты лох");
                showBombs();
                stopGame();
                return;
            }

            openedTiles++;
            isOpened = true;
            text.setVisible(true);
            border.setFill(null);

            if (openedTiles == (xTiles * yTiles) - allBombs) {
                info.setText("Красава");
                showBombs();
                stopGame();
                flags.setText(": 0");
                return;
            }

            if (text.getText().isEmpty()) {
                getNeighbors(this).forEach(Tile::open);
            }
        }
    }



    @Override
    public void start(Stage stage) {
        scene = new Scene(createContent());
        stage.setScene(scene);
        stage.show();
        stage.setTitle("Sweeper");
        stage.setResizable(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
