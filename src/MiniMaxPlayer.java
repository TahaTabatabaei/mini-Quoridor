import java.util.*;

public class MiniMaxPlayer extends Player{

    private final int d = 3;
    private double MAX_DEPTH = d+0.0;
    private double INFINITY = 9999.0;

    public MiniMaxPlayer(String color, int x, int y, Board board) {
        super(color, x, y, board);
    }


    public String bfs(MiniMaxPlayer opponent){
        double self_distance = 0.0;
        double opponent_distance = 0.0;
        Set<MiniMaxPlayer> players = new HashSet<MiniMaxPlayer>();
        players.add(this);
        players.add(opponent);

        Set<Piece> destination = new HashSet<Piece>();
        for (MiniMaxPlayer player : players) {
            if (player.color.equals("white")) destination = this.board.get_white_goal_pieces();
            else destination = this.board.get_black_goal_pieces();

            Queue<Piece> queue = new LinkedList<Piece>();
            HashMap<Piece, Boolean> visited = new HashMap<Piece, Boolean>();
            HashMap<Piece, Double> distances = new HashMap<Piece, Double>();

            for (int y = 0; y < this.board.ROWS_NUM; y++) {
                for (int x = 0; x < this.board.COLS_NUM; x++) {
                    visited.put(this.board.boardMap[y][x], false);
                    distances.put(this.board.boardMap[y][x], this.INFINITY);
                }
            }

            String player_pos = player.get_position();
            int x = Integer.parseInt(player_pos.split(",")[0]);
            int y = Integer.parseInt(player_pos.split(",")[1]);
            Piece player_piece = this.board.get_piece(x , y);

            queue.add(player_piece);
            visited.put(player_piece, true);
            distances.put(player_piece, 0.0);

            while (queue.size() != 0){
                Piece piece = ((LinkedList<Piece>) queue).removeFirst();

                Set<Piece> piece_temp = new HashSet<Piece>();

                piece_temp = this.board.get_piece_neighbors(piece);
                for (Piece p : piece_temp) {
                    if (!visited.get(p)){
                        double t = distances.get(piece) + 1;
                        distances.put(p, t);
                        visited.put(p, true);
                        queue.add(p);
                    }
                }

                double min_distance = this.INFINITY;

                for (Piece p_key : distances.keySet()) {
                    if (destination.contains(p_key)){
                        if (distances.get(p_key) < min_distance){
                            min_distance = distances.get(p_key);
                        }
                    }
                }

                if (player == this) self_distance = min_distance;
                else opponent_distance = min_distance;
            }
        }

        return self_distance + "," + opponent_distance;
    }

    public double evaluate(MiniMaxPlayer opponent){
        String distances = this.bfs(opponent);
//        System.out.println("dddd: " +distances);
        double self_distance = Double.parseDouble(distances.split(",")[0]);
        double opponent_distance  = Double.parseDouble(distances.split(",")[1]);

        double total_score = (5 * opponent_distance - self_distance) * (
                1 + this.walls_count / 2.0
                );

        return total_score;
    }

    /**
     * find a value from maxValue and then return the action related to the value
     * @param opponent
     * @return
     */
    public String get_best_action(MiniMaxPlayer opponent){
        Pair pair;
        pair = maxValue(opponent,  -(INFINITY), INFINITY,1);
        return pair.move;
    }


    private Pair maxValue(MiniMaxPlayer oppState, double alpha, double beta, int depth){
        // TODO find best action
//        System.out.println("######################################## " + depth);
        Pair pair;

        if (this.is_winner() || (depth >=d) ){
            return new Pair(this.evaluate(oppState),null);
        }
        double best_action_value = - (this.INFINITY);
        String best_move="";
        Set<String> legal_move = new HashSet<String>();
        legal_move = this.get_legal_actions(oppState);
        for (String action : legal_move) {
            this.play(action, true);

            double action_value = -999999;
            pair = minValue(oppState, alpha, beta, depth+1);
            action_value = pair.value;
//            System.out.println("value at min:" + action_value);

            this.undo_last_action();

            if (best_action_value < action_value){
                best_action_value = action_value;
                best_move = action;
                alpha = Math.max(alpha, best_action_value);
            }
            if (best_action_value >= beta){
//                System.out.println("beta cut:" + beta);
                return new Pair(best_action_value,best_move);
            }
        }
//        System.out.println("best at max: "+ best_action_value);
        return new Pair(best_action_value,best_move);
    }

    private Pair minValue(MiniMaxPlayer oppState, double alpha, double beta, int depth){
        // TODO find worse action
        // TODO opp or this?
//        System.out.println("***************************************** " + depth);
        Pair pair;
        if (this.is_winner() || (depth >=d) ){
            return new Pair(this.evaluate(oppState),null);
        }
        double best_action_value = (this.INFINITY);
        String best_move="";
        Set<String> legal_move = new HashSet<String>();
        legal_move = this.get_legal_actions(oppState);
        for (String action : legal_move) {
            this.play(action, true);

            double action_value=-999999;
            pair = maxValue(oppState, alpha, beta, depth+1);
            action_value = pair.value;
//            System.out.println("value at min:" + action_value);

            this.undo_last_action();

            if (best_action_value > action_value){
                best_action_value = action_value;
                best_move = action;
                beta = Math.min(beta, best_action_value);
            }
            if (best_action_value <= alpha){
//                System.out.println("alpha cut:" + alpha);
                return new Pair(best_action_value,best_move);
            }
        }

//        System.out.println("best at min: "+ best_action_value);
        return new Pair(best_action_value,best_move);
    }

}
