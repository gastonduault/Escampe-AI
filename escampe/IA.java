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

    private static final int N_INIT = 200;  // Nombre d'itérations pour le placement initial
    private static final int PROF_INIT = 3; // Profondeur minimax
    private int profondeurMax = PROF_INIT; // Profondeur max minimax

    private int couleur;
    private EscampeBoard board;
    private final Random random = new Random();
    private boolean initialized = false; // premier coup ?
    private String dernierMouvementEnnemi = "";

    // Piece-Square Tables pour la valeur positionnelle des pièces
    private static final int[][] PST_PALADIN = {
            { 1, 2, 3, 3, 2, 1},
            { 2, 4, 5, 5, 4, 2},
            { 3, 5, 6, 6, 5, 3},
            { 3, 5, 6, 6, 5, 3},
            { 2, 4, 5, 5, 4, 2},
            { 1, 2, 3, 3, 2, 1}
    };
    private static final int[][] PST_LICORNE = {
            { 0, 0, 1, 1, 0, 0},
            { 0, 2, 3, 3, 2, 0},
            { 1, 3, 4, 4, 3, 1},
            { 1, 3, 4, 4, 3, 1},
            { 0, 2, 3, 3, 2, 0},
            { 0, 0, 1, 1, 0, 0}
    };

    private static final String[] BLACK_FORMATIONS = {
            // Base
            "C1/B2/C2/D1/E2/F2",
            // Formation Central
            "D1/B1/C2/D2/E2/F2",
            // Formation Agressive à gauche
            "B1/A2/B2/C2/E1/F2",
            // Formation Agressive à droite
            "F1/F2/E2/D2/B1/C2",
            // Formation Écartée
            "C1/A2/C2/F2/E2/D1",
    };

<<<<<<< HEAD
    // Initialisation et gestion du jeu
=======
>>>>>>> 90e0325a48f21b36aaab81cd8eb3110aeabe0056
    @Override
    public void initJoueur(int mycolour) {
        this.couleur = mycolour;
        this.board = new EscampeBoard();
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
        return "Best player :)";
    }

    @Override
    public int getNumJoueur() {
        return this.couleur;
    }

    // Heuristique de placement qui utilise minmax
    public String getPlacementInitial() {
        EscampeBoard plateauDeBase = board.copie();
        int nbPieces = 0;
        for (int i = 0; i < EscampeBoard.HAUTEUR; i++) {
            for (int j = 0; j < EscampeBoard.LARGEUR; j++) {
                if (plateauDeBase.getBoard()[i][j] != EscampeBoard.VIDE) {
                    nbPieces++;
                }
            }
        }
        boolean premier = (nbPieces == 0);

        if (premier) {
            // Placement par défaut si pièces NOIRES parmi formations aléatoires
            return BLACK_FORMATIONS[random.nextInt(BLACK_FORMATIONS.length)];
        } else {
            boolean bas = board.coteChoisi(couleur).equals("bas");
            int[] lignes = bas ? new int[]{1,2} : new int[]{6,5};
            List<String> autorise = collectEmptyPositions(plateauDeBase, lignes);

            String meilleurCoup = simpleHeuristicPlacementForSide(bas ? "bas" : "haut");
            int meilleurScore = Integer.MIN_VALUE;

            for (int it = 0; it < N_INIT; it++) {
                String placement = randomPlacement(autorise, 6);
                EscampeBoard sim = plateauDeBase.copie();
                sim.appliquerCoup(placement, couleur);
                int score = minimax(sim, PROF_INIT, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
                if (score > meilleurScore) {
                    meilleurScore = score;
                    meilleurCoup = placement;
                }
            }
            return meilleurCoup;
            // return "A6/D6/E6/B5/C6/D5";
        }
    }

    // Heuristique stratégique défensive utilisée comme fallback
    private String simpleHeuristicPlacementForSide(String side) {
        boolean bas = side.equals("bas");
        String licorne = bas ? "C1" : "C6";
        String p1 = bas ? "B1" : "B6";
        String p2 = bas ? "D1" : "D6";
        String p3 = bas ? "B2" : "B5";
        String p4 = bas ? "D2" : "D5";
        String p5 = bas ? "C2" : "C5";
        return String.join("/", licorne, p1, p2, p3, p4, p5);
    }

<<<<<<< HEAD
    // Choix de mouvement
=======
>>>>>>> 90e0325a48f21b36aaab81cd8eb3110aeabe0056
    @Override
    public String choixMouvement() {
        System.out.println("------------------------------------------------------");
        if (!this.initialized) {
            this.initialized = true;
            String placement = getPlacementInitial();
            System.out.println("intialisation : " + placement);
            boolean success = board.appliquerCoup(placement, couleur);
            System.out.println("coup appliqué : " + success);
            return placement;
        }
        board.afficher();
        List<String> coupsPossibles = board.getCoupsPossibles(couleur, dernierMouvementEnnemi);
        if (coupsPossibles.isEmpty()) return "E";

        Collections.shuffle(coupsPossibles, random);

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

    private List<String> collectEmptyPositions(EscampeBoard board, int[] lignes) {
        List<String> autorise = new ArrayList<>();
        for (char c = 'A'; c <= 'F'; c++) {
            for (int l : lignes) {
                if (board.getBoard()[l-1][c-'A'] == EscampeBoard.VIDE) {
                    autorise.add("" + c + l);
                }
            }
        }
        return autorise;
    }

    private String randomPlacement(List<String> positions, int count) {
        Set<String> used = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            String pick;
            do {
                pick = positions.get(random.nextInt(positions.size()));
            } while (used.contains(pick));
            used.add(pick);
            if (i > 0) sb.append("/");
            sb.append(pick);
        }
        return sb.toString();
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
                    score += pstValue(piece, i, j);
                }
            }
        }

        // Distance quadratique à la licorne ennemie
        int[] posLicorneEnnemie = trouverLicorneEnnemie(plateau);
        if (posLicorneEnnemie != null) {
            int distance = distanceVersLicorne(plateau, posLicorneEnnemie);
            score += Math.max(0, 50 - 5 * distance * distance);
        }

        // Contrôle de zone élargi
        score += evaluerControleZone(plateau);

        // Mobilité relative
        score += evaluerMobilite(plateau);

        // Sécurité de ma licorne
        score += evaluerSecuriteLicorne(plateau);

        // Contrôle des cases centrales
        // score += evaluerControleCentral(plateau);

        return score;
    }

    // Heuristique de mobilité : différentiel des coups possibles
    private int evaluerMobilite(EscampeBoard plateau) {
        List<String> coupsPerso = plateau.getCoupsPossibles(couleur, dernierMouvementEnnemi);
        List<String> coupsAdversaire = plateau.getCoupsPossibles(-couleur, dernierMouvementEnnemi);
        return (coupsPerso.size() - coupsAdversaire.size()) * 5;
    }

    //  Calcul du nombre d'échappatoires (coups possibles) pour la licorne
    private int nombreEchappatoiresLicorne(EscampeBoard plateau) {
        int[] myLic = trouverMaLicorne(plateau);
        if (myLic == null) return 0;
        String licStr = coordToString(myLic[1], myLic[0]);
        int count = 0;
        for (String m : plateau.getCoupsPossibles(couleur, dernierMouvementEnnemi)) {
            if (m.startsWith(licStr + "-")) count++;
        }
        return count;
    }

    //  Heuristique de sécurité : pénalise une licorne avec peu de fuites
    private int evaluerSecuriteLicorne(EscampeBoard plateau) {
        int fuites = nombreEchappatoiresLicorne(plateau);
        if (fuites <= 2) return -50;
        if (fuites <= 4) return -20;
        return fuites * 2;
    }

    // Heuristique de contrôle de zone : cases à distance ≤2 du centre
    private int evaluerControleZone(EscampeBoard plateau) {
        int score = 0;
        int centreI = EscampeBoard.HAUTEUR/2 - 1;
        int centreJ = EscampeBoard.LARGEUR/2 - 1;
        for (int i = 0; i < EscampeBoard.HAUTEUR; i++) {
            for (int j = 0; j < EscampeBoard.LARGEUR; j++) {
                int d = Math.abs(i - centreI) + Math.abs(j - centreJ);
                if (d <= 2 && plateau.getBoard()[i][j] != EscampeBoard.VIDE) {
                    int w = (d <= 1 ? 3 : 1);
                    int signe = plateau.getBoard()[i][j] * couleur > 0 ? 1 : -1;
                    score += signe * w;
                }
            }
        }
        return score;
    }

    private int pstValue(int piece, int i, int j) {
        if (Math.abs(piece) == 1)
            return (piece * couleur > 0 ? 1 : -1) * PST_PALADIN[i][j];
        if (Math.abs(piece) == 2)
            return (piece * couleur > 0 ? 1 : -1) * PST_LICORNE[i][j] * 5;
        return 0;
    }

    private int[] trouverLicorneEnnemie(EscampeBoard plateau) {
        for (int i = 0; i < EscampeBoard.HAUTEUR; i++) {
            for (int j = 0; j < EscampeBoard.LARGEUR; j++) {
                if (plateau.getBoard()[i][j] == (couleur == EscampeBoard.BLANC
                        ? EscampeBoard.LICORNENOIRE
                        : EscampeBoard.LICORNEBLANCHE)) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    // Localise ma propre licorne sur le plateau
    private int[] trouverMaLicorne(EscampeBoard plateau) {
        int target = (couleur == EscampeBoard.BLANC
                ? EscampeBoard.LICORNEBLANCHE : EscampeBoard.LICORNENOIRE);
        for (int i = 0; i < EscampeBoard.HAUTEUR; i++) {
            for (int j = 0; j < EscampeBoard.LARGEUR; j++) {
                if (plateau.getBoard()[i][j] == target) return new int[]{i, j};
            }
        }
        return null;
    }

    // indices (col,row) en notation plateau ("A1", "B3", ...)
    private String coordToString(int col, int row) {
        return String.valueOf((char)('A' + col)) + (row + 1);
    }

    // Calcule la distance de Manhattan d'un paladin à la position donnée
    private int distanceVersLicorne(EscampeBoard plateau, int[] posLicorne) {
        int minDist = Integer.MAX_VALUE;
        int paladinType = (couleur == EscampeBoard.BLANC)
                ? EscampeBoard.PALADINBLANC
                : EscampeBoard.PALADINNOIR;
        int[][] b = plateau.getBoard();
        for (int i = 0; i < EscampeBoard.HAUTEUR; i++) {
            for (int j = 0; j < EscampeBoard.LARGEUR; j++) {
                if (b[i][j] == paladinType) {
                    int d = Math.abs(i - posLicorne[0]) + Math.abs(j - posLicorne[1]);
                    minDist = Math.min(minDist, d);
                }
            }
        }
        return (minDist == Integer.MAX_VALUE) ? 20 : minDist;
    }

    // Ancienne heuristique de contrôle central
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
}