// javac escampe/*.java
// java -cp escampeobf.jar escampe.ServeurJeu 1234 1
// java -cp .:escampeobf.jar escampe.ClientJeu escampe.IA localhost 1234
// java -cp escampeobf.jar escampe.ClientJeu escampe.JoueurAleatoire localhost 1234

package escampe;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class IA implements IJoueur {
    private int couleur;
    private EscampeBoard board;
    private Random random = new Random();
    private int profondeurMax = 3;
    private boolean initalized = false;
    private String dernierMouvementEnnemi = "";
    ;

    private String getPlacementInitial() {
        String positionAdversaire = board.coteChoisi(couleur);

        if(positionAdversaire.equals("vide")) { // on est joueur noir on choisi car le plateau est vide
            return "E1/F1/A1/E2/C2/A2";
        }

        // Liste des cases autorisées
        List<String> autorise = new ArrayList<>();
        int[] lignes = positionAdversaire.equals("bas") ? new int[]{1,2} : new int[]{6,5};
        for (char c = 'A'; c <= 'F'; c++) {
            for (int l : lignes) {
                autorise.add(""+c+l);
            }
        }

        final int N = 200;
        Random rnd = new Random();
        String meilleurPlacement = simpleHeuristicPlacement(); // backup si rien de mieux
        int meilleurScore = Integer.MIN_VALUE;

        for (int iter = 0; iter < N; iter++) {
            // Choisir une licorne parmi les 12 cases
            String licorne = autorise.get(rnd.nextInt(autorise.size()));

            // Choisir 5 paladins distincts sur le reste
            Set<String> rest = new HashSet<>(autorise);
            rest.remove(licorne);
            List<String> restList = new ArrayList<>(rest);
            Collections.shuffle(restList, rnd);
            List<String> paladins = restList.subList(0, 5);

            // Construire la chaîne de placement
            StringBuilder sb = new StringBuilder(licorne);
            for (String p : paladins) sb.append("/").append(p);
            String placement = sb.toString();

            // Évaluer par Minimax profondeur 2
            EscampeBoard copie = new EscampeBoard();
            copie.getBoard();
            copie.appliquerCoup(placement, couleur);
            int score = minimax(copie, 3, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

            if (score > meilleurScore) {
                meilleurScore = score;
                meilleurPlacement = placement;
            }
        }

        return meilleurPlacement;
    }

    /** Heuristique défensive simple par défaut */
    private String simpleHeuristicPlacement() {
        boolean bas = board.coteChoisi(couleur).equals("bas");
        String licorne = bas ? "C1" : "C6";
        String p1 = bas ? "B1" : "B6";
        String p2 = bas ? "D1" : "D6";
        String p3 = bas ? "B2" : "B5";
        String p4 = bas ? "D2" : "D5";
        String p5 = bas ? "C2" : "C5";
        return String.join("/", licorne, p1, p2, p3, p4, p5);
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
        if (coupsPossibles.isEmpty()) return "E";

        String meilleurCoup = coupsPossibles.get(0);
        int meilleurScore = Integer.MIN_VALUE;

        for (String coup : coupsPossibles) {
            EscampeBoard copie = board.copie();
            copie.appliquerCoup(coup, couleur);
            int score = minimax(copie, profondeurMax - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

            if (score > meilleurScore) { // si meilleur score on prend cette branche
                meilleurScore = score;
                meilleurCoup = coup;
            }
        }

        board.appliquerCoup(meilleurCoup, couleur);
        return meilleurCoup;
    }

    private int minimax(EscampeBoard plateau, int profondeur, int alpha, int beta, boolean maximisant) {
        if (profondeur == 0 || plateau.estPartieTerminee()) {
            return evaluerPositionAvancee(plateau);
        }

        if (maximisant) {
            int maxEval = Integer.MIN_VALUE;
            List<String> coups = plateau.getCoupsPossibles(couleur, dernierMouvementEnnemi);

            for (String coup : coups) {
                EscampeBoard copie = plateau.copie();
                copie.appliquerCoup(coup, couleur);
                int eval = minimax(copie, profondeur - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            List<String> coups = plateau.getCoupsPossibles(-couleur, dernierMouvementEnnemi);

            for (String coup : coups) {
                EscampeBoard copie = plateau.copie();
                copie.appliquerCoup(coup, -couleur);
                int eval = minimax(copie, profondeur - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private int[] trouverLicorneEnnemie(EscampeBoard plateau) {
        for (int i = 0; i < EscampeBoard.HAUTEUR; i++) {
            for (int j = 0; j < EscampeBoard.LARGEUR; j++) {
                if (plateau.getBoard()[i][j] == (couleur == EscampeBoard.BLANC ?
                        EscampeBoard.LICORNENOIRE : EscampeBoard.LICORNEBLANCHE)) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    private int evaluerControleCentral(EscampeBoard plateau) {
        int score = 0;
        int[][] zonesCentrales = {{2,2}, {2,3}, {3,2}, {3,3}};
        for (int[] pos : zonesCentrales) {
            int piece = plateau.getBoard()[pos[0]][pos[1]];
            if (piece != EscampeBoard.VIDE) {
                score += (piece * couleur > 0) ? 3 : -3;
            }
        }
        return score;
    }

    private int distanceVersLicorne(EscampeBoard plateau, int[] posLicorneEnnemie) {
        int minDistance = Integer.MAX_VALUE;
        int[][] board = plateau.getBoard();

        // Type de paladin à rechercher selon la couleur
        int paladinType = (couleur == EscampeBoard.BLANC) ? EscampeBoard.PALADINBLANC : EscampeBoard.PALADINNOIR;

        for (int i = 0; i < EscampeBoard.HAUTEUR; i++) {
            for (int j = 0; j < EscampeBoard.LARGEUR; j++) {
                if (board[i][j] == paladinType) {
                    // Calcul distance de Manhattan
                    int distance = Math.abs(i - posLicorneEnnemie[0]) + Math.abs(j - posLicorneEnnemie[1]);
                    minDistance = Math.min(minDistance, distance);
                }
            }
        }

        return (minDistance == Integer.MAX_VALUE) ? 20 : minDistance; // 20 = valeur de repli
    }

    // Modification dans evaluerPositionAvancee()
    private int evaluerPositionAvancee(EscampeBoard plateau) {
        int score = 0;
        int[][] board = plateau.getBoard();

        // Valeur des pièces
        for (int i = 0; i < EscampeBoard.HAUTEUR; i++) {
            for (int j = 0; j < EscampeBoard.LARGEUR; j++) {
                int piece = board[i][j];
                if (piece != EscampeBoard.VIDE) {
                    // Attribution de valeurs (Licorne > Paladin)
                    int valeur = (Math.abs(piece) == 2) ? 100 : 10;
                    score += (piece * couleur > 0) ? valeur : -valeur;
                }
            }
        }

        // Distance à la licorne ennemie
        int[] posLicorneEnnemie = trouverLicorneEnnemie(plateau);
        if (posLicorneEnnemie != null) {
            int distance = distanceVersLicorne(plateau, posLicorneEnnemie);
            score += (30 - distance * 3); // +30 points si distance=0, décroît linéairement
        }

        // Contrôle des cases centrales
        score += evaluerControleCentral(plateau);

        return score;
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