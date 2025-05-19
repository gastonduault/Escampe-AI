// javac escampe/*.java
// java -cp escampeobf.jar escampe.ServeurJeu 1234 1
// java -cp .:escampeobf.jar escampe.ClientJeu escampe.IA localhost 1234
// java -cp escampeobf.jar escampe.ClientJeu escampe.JoueurAleatoire localhost 1234

package escampe;

import java.util.List;
import java.util.Random;

public class IA implements IJoueur {
    private int couleur;
    private EscampeBoard board;
    private Random random = new Random();
    private int profondeurMax = 3; // Profondeur de recherche pour l'IA
    private boolean initalized = false;
    private String dernierMouvementEnnemi = "";
    ;

    private String getPlacementInitial() {
        if (board.coteChoisi(couleur).equals("bas")) {
            return "C1/A2/B1/D2/E1/F2";
        } else {
            // Noirs placent sur lignes 5-6
            // Licorne en C6 (ligne 6), paladins autour
            return "C6/A5/B6/D5/E6/F5";
        }
    }

    @Override
    public String choixMouvement() {
        System.out.println("------------------------------------------------------");
        if (!this.initalized) {
            this.initalized = true;
            String placement = getPlacementInitial();
            System.out.println("intialisation : " + placement);
            boolean success = board.appliquerCoup(placement, couleur);
            System.out.println("coup appliqué : " + success);
            return placement;
        }
        board.afficher();
        List<String> coupsPossibles = board.getCoupsPossibles(couleur, dernierMouvementEnnemi);

        if (coupsPossibles.isEmpty()) {
            System.out.println("Aucuns coups possible");
            return "E";
        }

        String meilleurCoup = coupsPossibles.get(0);
        System.out.println("meilleurCoup : " + meilleurCoup);
        System.out.println("Nombre de coup possible : " + coupsPossibles);
        int meilleurScore = Integer.MIN_VALUE;

        boolean success = board.appliquerCoup(meilleurCoup, couleur);
        System.out.println("coup appliqué : " + success);
        int score = minimax(board, profondeurMax - 1, false);

        return meilleurCoup;
    }

    private int minimax(EscampeBoard plateau, int profondeur, boolean maximisant) {
        if (profondeur == 0 || plateau.estPartieTerminee()) {
            return plateau.evaluerPosition(couleur);
        }

        if (maximisant) {
            int maxEval = Integer.MIN_VALUE;
            List<String> coups = plateau.getCoupsPossibles(couleur, this.dernierMouvementEnnemi);

            for (String coup : coups) {
                EscampeBoard copie = plateau.copie();
                copie.appliquerCoup(coup, couleur);
                int eval = minimax(copie, profondeur - 1, false);
                maxEval = Math.max(maxEval, eval);
            }

            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            List<String> coups = plateau.getCoupsPossibles(-couleur, this.dernierMouvementEnnemi);

            for (String coup : coups) {
                EscampeBoard copie = plateau.copie();
                copie.appliquerCoup(coup, -couleur);
                int eval = minimax(copie, profondeur - 1, true);
                minEval = Math.min(minEval, eval);
            }

            return minEval;
        }
    }

    @Override
    public void initJoueur(int mycolour) {
        this.couleur = mycolour;
        this.board = new EscampeBoard();
        // Si c'est le premier joueur (Blanc), on initialise un plateau vide
        if (mycolour == BLANC) {
            for (int i = 0; i < EscampeBoard.HAUTEUR; i++) {
                for (int j = 0; j < EscampeBoard.LARGEUR; j++) {
                    board.getBoard()[i][j] = EscampeBoard.VIDE;
                }
            }
        }
    }

    @Override
    public void mouvementEnnemi(String coup) {
        System.out.println("mouvementEnnemi : " + coup);
        this.dernierMouvementEnnemi = "";
        if (!coup.contains("/") && !coup.equals("E")) {
            String[] coups = coup.split("-");
            this.dernierMouvementEnnemi = coups[1];
        }
        boolean success = board.appliquerCoup(coup, -couleur);
        System.out.println("coup appliqué : " + success);
    }


    @Override
    public void declareLeVainqueur(int colour) {
        System.out.println("Le vainqueur est : " + (colour == BLANC ? "Blanc" : "Noir"));
    }

    @Override
    public String binoName() {
        return "Deepseek";
    }

    @Override
    public int getNumJoueur() {
        return this.couleur;
    }

}