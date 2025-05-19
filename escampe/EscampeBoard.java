package escampe;

import java.util.*;

public class EscampeBoard {

    static final int BLANC = -1;
    static final int NOIR = 1;

    // Constantes pour les pièces
    public static final int LICORNEBLANCHE = -2;
    public static final int PALADINBLANC = -1;
    public static final int VIDE = 0;
    public static final int PALADINNOIR = 1;
    public static final int LICORNENOIRE = 2;

    // Dimensions du plateau
    public static final int LARGEUR = 6;
    public static final int HAUTEUR = 6;

    private static final int[][] LISERES = {
            {1, 2, 2, 3, 1, 2},
            {3, 1, 3, 1, 3, 2},
            {2, 3, 1, 2, 1, 3},
            {2, 1, 3, 2, 3, 1},
            {1, 3, 1, 3, 1, 2},
            {3, 2, 2, 1, 3, 2}
    };

    // Le plateau de jeu (lignes x colonnes)
    private int[][] board;

    public EscampeBoard() {
        board = new int[HAUTEUR][LARGEUR];
        for (int i = 0; i < HAUTEUR; i++) {
            for (int j = 0; j < LARGEUR; j++) {
                board[i][j] = VIDE;
            }
        }
    }

    public boolean appliquerCoup(String coup, int couleur) {
        if (coup.equals("E")) {
            return !estPlateauVide(); // On ne peut passer que si le plateau n'est pas vide
        }

        if (coup.contains("/")) {
            return appliquerPlacementInitial(coup, couleur);
        }

        try {
            String[] parts = coup.split("-");
            if (parts.length != 2) return false;

            String from = parts[0];
            String to = parts[1];

            int fromCol = from.charAt(0) - 'A';
            int fromRow = Integer.parseInt(from.substring(1)) - 1;
            int toCol = to.charAt(0) - 'A';
            int toRow = Integer.parseInt(to.substring(1)) - 1;

            // Vérifie que la case de départ contient une pièce de la bonne couleur
            int piece = board[fromRow][fromCol];
            if ((couleur == BLANC && piece > 0) || (couleur == NOIR && piece < 0)) {
                return false;
            }

            // Vérifie que la case d'arrivée est vide ou contient une pièce ennemie
            if (board[toRow][toCol] != VIDE &&
                    ((couleur == BLANC && board[toRow][toCol] < 0) ||
                            (couleur == NOIR && board[toRow][toCol] > 0))) {
                return false;
            }

            // Effectue le mouvement
            board[toRow][toCol] = board[fromRow][fromCol];
            board[fromRow][fromCol] = VIDE;

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public EscampeBoard copie() {
        EscampeBoard copie = new EscampeBoard();
        for (int i = 0; i < HAUTEUR; i++) {
            System.arraycopy(this.board[i], 0, copie.board[i], 0, LARGEUR);
        }
        return copie;
    }

    public void afficher() {
        System.out.println("   A  B  C  D  E  F");
        for (int i = 0; i < HAUTEUR; i++) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < LARGEUR; j++) {
                switch (board[i][j]) {
                    case LICORNEBLANCHE: System.out.print("LB"); break;
                    case PALADINBLANC: System.out.print("PB"); break;
                    case VIDE: System.out.print(" ."); break;
                    case PALADINNOIR: System.out.print("PN"); break;
                    case LICORNENOIRE: System.out.print("LN"); break;
                }
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    public int[][] getBoard() {
        return board;
    }

    public boolean checkLisere(String ennemi, String position) {
        if(!ennemi.isEmpty()) {
            int lisere = LISERES[ Integer.parseInt(ennemi.substring(1)) - 1][ennemi.charAt(0) - 'A'];
            int pos = LISERES[ Integer.parseInt(position.substring(1)) - 1][position.charAt(0) - 'A'];
            return pos == lisere;
        }
        return true;
    }

    public boolean notrePiece(int couleur, int piece) {
        return (couleur == BLANC && piece < 0) || (couleur == NOIR && piece > 0);
    }

    public List<String> getCoupsPossibles(int couleur, String mouvementEnnemi) {
        List<String> coups = new ArrayList<>();
        int adversaire = PALADINBLANC;
        if(couleur == PALADINBLANC) {
            adversaire = PALADINNOIR;
        }

        for (int row = 0; row < HAUTEUR; row++) {
            for (int col = 0; col < LARGEUR; col++) {
                int piece = board[row][col];
                String position = "" + (char)('A' + col) + (row + 1);
                if (notrePiece(couleur, piece) && checkLisere(mouvementEnnemi, position)) {
                    coups.addAll(getDeplacementsPossibles(position, couleur));
                }
            }
        }
        if (coups.isEmpty()) {
            coups.add("E");
        }

        return coups;
    }

    private List<String> getDeplacementsPossibles(String position, int couleur) {
        List<String> deplacements = new ArrayList<>();
        int col = position.charAt(0) - 'A';
        int row = Integer.parseInt(position.substring(1)) - 1;
        int piece = board[row][col];
        int lisere = LISERES[row][col];
        boolean isPaladin = Math.abs(piece) == PALADINNOIR;

        // Directions : Haut, Bas, Gauche, Droite
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        Queue<MovementState> queue = new LinkedList<>();
        queue.add(new MovementState(row, col, 0, -1, new ArrayList<>()));

        while (!queue.isEmpty()) {
            MovementState current = queue.poll();

            // Vérifier si on a atteint le nombre de pas
            if (current.steps == lisere) {
                String to = String.format("%c%d", (char) ('A' + current.col), current.row + 1);
                if (isValidTarget(current.row, current.col, couleur, isPaladin)) {
                    deplacements.add(position + "-" + to);
                }
                continue;
            }

            // Explorer les directions possibles
            for (int dirIdx = 0; dirIdx < directions.length; dirIdx++) {
                int[] dir = directions[dirIdx];

                // Vérifier l'inversion de direction
                if (current.lastDir != -1 && isOpposite(dir, directions[current.lastDir])) {
                    continue;
                }

                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];

                // Vérifier les limites du plateau et les cases intermédiaires
                if (newRow < 0 || newRow >= HAUTEUR || newCol < 0 || newCol >= LARGEUR) continue;
                if (!checkIntermediateCells(current.path, newRow, newCol)) continue;

                // Créer le nouveau chemin et ajouter à la file
                List<int[]> newPath = new ArrayList<>(current.path);
                newPath.add(new int[]{newRow, newCol});
                queue.add(new MovementState(newRow, newCol, current.steps + 1, dirIdx, newPath));
            }
        }

        return deplacements;
    }

    // Vérifie si la direction est opposée
    private boolean isOpposite(int[] dir1, int[] dir2) {
        return dir1[0] == -dir2[0] && dir1[1] == -dir2[1];
    }

    // Vérifie les cases intermédiaires
    private boolean checkIntermediateCells(List<int[]> path, int newRow, int newCol) {
        for (int[] pos : path) {
            if (pos[0] == newRow && pos[1] == newCol) return false; // Évite les répétitions
            if (board[pos[0]][pos[1]] != VIDE) return false;
        }
        return true;
    }

    // Validation de la case d'arrivée
    private boolean isValidTarget(int row, int col, int couleur, boolean isPaladin) {
        int target = board[row][col];
        if (isPaladin) {
            return target == VIDE || (target * couleur < 0 && Math.abs(target) == LICORNENOIRE);
        } else {
            return target == VIDE;
        }
    }

    // Classe helper pour stocker l'état du mouvement
    private static class MovementState {
        int row, col, steps, lastDir;
        List<int[]> path;

        MovementState(int row, int col, int steps, int lastDir, List<int[]> path) {
            this.row = row;
            this.col = col;
            this.steps = steps;
            this.lastDir = lastDir;
            this.path = path;
        }
    }

    public boolean estPartieTerminee() {
        boolean blancALicorne = false;
        boolean noirALicorne = false;

        for (int[] ligne : board) {
            for (int piece : ligne) {
                if (piece == LICORNEBLANCHE) blancALicorne = true;
                if (piece == LICORNENOIRE) noirALicorne = true;
            }
        }

        return !blancALicorne || !noirALicorne;
    }

    /**
     * Évalue la position actuelle (pour l'IA)
     */
    public int evaluerPosition(int couleurJoueur) {
        int score = 0;

        for (int[] ligne : board) {
            for (int piece : ligne) {
                if (piece != VIDE) {
                    int valeur = Math.abs(piece) * (piece * couleurJoueur > 0 ? 1 : -1);
                    score += valeur;
                }
            }
        }

        return score;
    }

    public boolean estPlateauVide() {
        for (int i = 0; i < HAUTEUR; i++) {
            for (int j = 0; j < LARGEUR; j++) {
                if(board[i][j] != VIDE) {
                    return false;
                }
            }
        }
        return true;
    }

    // Cote choisi par l'adversaire
    public String coteChoisi(int couleur) {
        int adversaire = PALADINBLANC;
        if(couleur == PALADINBLANC) {
            adversaire = PALADINNOIR;
        }

        for (int i = 0; i < HAUTEUR; i++) {
            for (int j = 0; j < LARGEUR; j++) {
                if(board[i][j] == adversaire && (i == 4 || i == 5)) {
                    System.out.println("L'adversaire a choisi le côté bas");
                    return "bas";
                }
            }
        }
        System.out.println("L'adversaire a choisi le côté haut");
        return "haut";
    }

    public boolean appliquerPlacementInitial(String placement, int couleur) {
        String[] positions = placement.split("/");

        try {
            if (positions.length != 6) {
                System.out.println("manque des pieces : " + positions.length);
                return false;
            }

            // Licorne
            String licornePos = positions[0];
            int licorneCol = licornePos.charAt(0) - 'A';
            int licorneRow = Integer.parseInt(licornePos.substring(1)) - 1;
            board[licorneRow][licorneCol] = (couleur == BLANC) ? LICORNEBLANCHE : LICORNENOIRE;

            // Paladins
            for (int i = 1; i < positions.length; i++) {
                String pos = positions[i];
                int col = pos.charAt(0) - 'A';
                int row = Integer.parseInt(pos.substring(1)) - 1;

                board[row][col] = (couleur == BLANC) ? PALADINBLANC : PALADINNOIR;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}