/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package travail_pratique_3;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Classe Principale - Travail_Pratique_3
 * Complete l'application principale JavaFX par un jeu de reflexion... et de boules.
 * @author carm
 */
public class Travail_Pratique_3 extends Application {
    
    /**
     * Largeur de la Grille de jeu.
     */
    private static final int W = 6;
    /**
     * Hauteur de la Grille de jeu.
     */
    private static final int H = 6;
    /**
     * Taille d'une boule à l'unite.
     */
    private static final int SIZE = 100;
    /**
     * Hauteur de l'Entete de l'affichage.
     */
    private static final int HEADER_HEIGHT = 35;
    
    
    /**
     * Liste de tout les sons de victoires possibles.
     */
    private List<AudioClip> scoringClips;
    
    /**
     * Tableau de toutes les couleurs possibles en jeu.
     */
    private Color[] colors = new Color[] {
            Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW
    };
    
    /**
     * Boule selectionnee actuellement.
     */
    private Boule selected = null;
    /**
     * Liste de toutes les boules sur la Grille de jeu.
     */
    private List<Boule> Tab_Boules;
    
    /**
     * Score actuel de la partie en cours.
     */
    private IntegerProperty score = new SimpleIntegerProperty();
    
    /**
     * Creation du contenu initial de la scène de jeu.
     * @return Scene de jeu sous forme abstraite
     */
    private Parent createContent()
    {
        // Cree l'affichage de jeu actuel
        BorderPane root = new BorderPane();
        root.setPrefSize(W * SIZE, H * SIZE + 100);
        
        // Initalise les listes et tableaux necessaires au deroulement du jeu
        Tab_Boules = IntStream.range(0, W * H)
                .mapToObj(i -> new Point2D(i % W, i / W))
                .map(Boule::new)
                .collect(Collectors.toList());
        scoringClips = Arrays.asList(
                new AudioClip(new File("ressources/audio/win1.mp3").toURI().toString()),
                new AudioClip(new File("ressources/audio/win2.mp3").toURI().toString()),
                new AudioClip(new File("ressources/audio/win3.mp3").toURI().toString()));
        
        // Ajoute le tableau de Boule en tant qu'enfant de l'affichage actuel
        root.getChildren().addAll(Tab_Boules);
        
        // Affiche le score
        Text textScore = new Text();
        textScore.setTranslateX(W * SIZE / 4);
        textScore.setTranslateY(H * SIZE + 90);
        textScore.setFont(Font.font(68));
        textScore.textProperty().bind(score.asString("Score: [%d]"));
        
        // Creation du Menu
        MenuItem restartItem = new MenuItem("Restart");
        restartItem.setOnAction(e -> {
            score.set(0);
            Tab_Boules.forEach(Boule::randomize);
        });
        restartItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_ANY));
        MenuItem quitItem = new MenuItem("Quit");
        quitItem.setOnAction(e -> Platform.exit());
        quitItem.setAccelerator(new KeyCodeCombination(KeyCode.ESCAPE));
        Menu toolbar = new Menu("File", null, restartItem, quitItem);
        Menu authorItem = new Menu("Author", null, new MenuItem("Bastien LEGOY"));
        MenuItem linkItem = new MenuItem("Repository");
        linkItem.setOnAction(e -> this.getHostServices().showDocument("https://github.com/TrueAbastien/Travail_Pratique_3"));
        Menu help = new Menu("?", null, authorItem, linkItem);
        VBox menuBar = new VBox(new MenuBar(toolbar, help));
        menuBar.setPrefHeight(HEADER_HEIGHT);
        root.setTop(menuBar);
        
        // Ajoute le score a l'affichage
        root.getChildren().add(textScore);
        
        return root;
    }
    
    /**
     * Verifie l'etat de la grille de jeu.
     */
    private void checkState() {
        Map<Integer, List<Boule>> rows = Tab_Boules.stream().collect(Collectors.groupingBy(Boule::getRow));
        Map<Integer, List<Boule>> columns = Tab_Boules.stream().collect(Collectors.groupingBy(Boule::getColumn));

        rows.values().forEach(this::checkCombo);
        columns.values().forEach(this::checkCombo);
    }

    /**
     * Recherche la totalite des combos effectues dans une direction donnee.
     * @param Tab_BoulesLine Tableau des boules present dans la direction analysee
     */
    private void checkCombo(List<Boule> Tab_BoulesLine) {
        Boule jewel = Tab_BoulesLine.get(0);
        long count = Tab_BoulesLine.stream().filter(j -> j.getColor() != jewel.getColor()).count();
        if (count == 0) {
            score.set(score.get() + 1000);
            Tab_BoulesLine.forEach(Boule::randomize);
            scoringClips.get(new Random().nextInt(scoringClips.size())).play(.5);
        }
    }
    
    /**
     * Echange les effets d'une boule avec une autre.
     * @param a Boule d'origine
     * @param b Cible de l'echange
     */
    private void swap(Boule a, Boule b) {
        Paint color = a.getColor();
        a.setColor(b.getColor());
        b.setColor(color);
    }
    
    /**
     * Demmare l'application dans son Thread.
     * @param primaryStage Stage principal de l'application
     * @throws Exception Relancement des erreurs de creation de l'application
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(createContent()));
        primaryStage.show();
    }
    
    /**
     * Classe Boule
     * Representation d'une boule sur la grille de jeu.
     * @author carm
     */
    private class Boule extends Parent
    {
        /**
         * Affichage de la Boule en jeu: cercle de taille fixe.
         */
        private Circle circle = new Circle(SIZE / 2);
        
        /**
         * Constructeur initialisant les caracteristiques et evenements lies a une boule dans le jeu.
         * @param point Position de la boule creee dans la grille de jeu
         */
        public Boule(Point2D point)
        {
            // Initialise les caracteristiques du cercle
            circle.setCenterX(SIZE / 2);
            circle.setCenterY(SIZE / 2);
            circle.setFill(colors[new Random().nextInt(colors.length)]);
            
            // Deplace le cercle a une position plus appropriee
            setTranslateX(point.getX() * SIZE);
            setTranslateY(point.getY() * SIZE + HEADER_HEIGHT);
            getChildren().add(circle);
            
            // Met en place le code lie au clique de la souris sur une boule
            setOnMouseClicked(event -> {
                if (selected == null) {
                    selected = this;
                }
                else {
                    swap(selected, this);
                    checkState();
                    selected = null;
                }
            });
        }
        
        /**
         * Donne a une boule une valeure aleatoire.
         */
        public void randomize() {
            circle.setFill(colors[new Random().nextInt(colors.length)]);
        }
        
        /**
         * Recherche l'index de la colonne auquelle la boule appartient.
         * @return Index resultant de la recherche
         */
        public int getColumn() {
            return (int)getTranslateX() / SIZE;
        }
        
        /**
         * Recherche l'index de la ligne auquelle la boule appartient.
         * @return Index resultant de la recherche
         */
        public int getRow() {
            return (int)(getTranslateY() - HEADER_HEIGHT) / SIZE;
        }
        
        /**
         * Change la couleur de la boule.
         * @param color Nouvelle couleur appliquee
         */
        public void setColor(Paint color) {
            circle.setFill(color);
        }
        
        /**
         * Donne la couleur actuelle de la boule.
         * @return Couleur actuelle
         */
        public Paint getColor() {
            return circle.getFill();
        }
    }
    
    /**
     * Fonction Principale
     * Initialise l'application et lance son fonctionnement.
     * @param args Arguments d'execution
     */
    public static void main(String[] args) {
        launch(args);
    }
}
