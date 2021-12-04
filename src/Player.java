import java.time.Period;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class Player {
    int x;
    int y;
    String color;
    int walls_count;
    Board board;
    int moves_count;
    private Queue<String> actions_logs = null ;

    public Player(String  color, int x, int y, Board board) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.walls_count = 10;
        this.board = board;
        this.actions_logs = new LinkedList<String>();
        this.moves_count = 0;
    }

    public String get_position(){
        return this.x + "," + this.y;
    }

    public void move(int x , int y){
        this.board.get_piece(this.x, this.y).state = "empty";

        this.x = x;
        this.y = y;

        this.board.get_piece(this.x, this.y).state = this.color;
    }

    public void put_wall(int x, int y, String orientation){


        this.walls_count -= 1;

        Piece piece = this.board.get_piece(x, y);
        if (orientation.equals("horizontal")){
            Piece neighbor_piece1 = this.board.get_piece((x + 1), y);
            Piece neighbor_piece2 = this.board.get_piece(x, (y + 1));
            Piece neighbor_piece3 = this.board.get_piece((x + 1), (y + 1));
            piece.d_side = "block";
            neighbor_piece1.d_side = "block";
            neighbor_piece2.u_side = "block";
            neighbor_piece3.u_side = "block";
            this.board.paired_block_pieces.put(piece, neighbor_piece1);
        }
        else if (orientation.equals("vertical")){
            Piece neighbor_piece1 = this.board.get_piece(x, (y + 1));
            Piece neighbor_piece2 = this.board.get_piece((x + 1), y);
            Piece neighbor_piece3 = this.board.get_piece((x + 1), (y + 1));
            piece.r_side = "block";
            neighbor_piece1.r_side = "block";
            neighbor_piece2.l_side = "block";
            neighbor_piece3.l_side = "block";
            this.board.paired_block_pieces.put(piece, neighbor_piece1);
        }
    }

    public void play(String command, boolean is_evaluating){
        if (!is_evaluating){
            this.moves_count += 1;
        }
        String[] split_cmd = command.split("#");

        if (split_cmd[0].equals("move")){
            int x = Integer.parseInt(split_cmd[1]);
            int y = Integer.parseInt(split_cmd[2]);
            this.actions_logs.add("move#" + this.x + "#" + this.y + "#" + x + "#" + y);
            this.move(x, y);
        }
        else {
            int x = Integer.parseInt(split_cmd[1]);
            int y = Integer.parseInt(split_cmd[2]);
            String orientation = split_cmd[3];
            this.actions_logs.add(command);
            this.put_wall(x, y, orientation);
        }
    }

    public void undo_last_action(){
        String last_action  = ((LinkedList<String>) actions_logs).removeLast();
        String[] splitted_command = last_action.split("#");

        if (splitted_command[0].equals("wall")){ this.remove_wall(last_action); }
        else {
            int x = Integer.parseInt(splitted_command[1]);
            int y = Integer.parseInt(splitted_command[2]);
            this.move(x, y);
        }
    }

    public void remove_wall(String command){

        this.walls_count += 1;

        String [] splitted_command = command.split("#");
        int x = Integer.parseInt(splitted_command[1]);
        int y = Integer.parseInt(splitted_command[2]);
        String orientation = splitted_command[3];

        Piece piece = this.board.get_piece(x, y);
        if (orientation.equals("horizontal")){
            Piece neighbor_piece1 = this.board.get_piece((x + 1), y);
            Piece neighbor_piece2 = this.board.get_piece(x, (y + 1));
            Piece neighbor_piece3 = this.board.get_piece((x + 1), (y + 1));
            piece.d_side = "free";
            neighbor_piece1.d_side = "free";
            neighbor_piece2.u_side = "free";
            neighbor_piece3.u_side = "free";
            this.board.paired_block_pieces.remove(piece, neighbor_piece1);
        }
        else if (orientation.equals("vertical")){
            Piece neighbor_piece1 = this.board.get_piece(x, (y + 1));
            Piece neighbor_piece2 = this.board.get_piece((x + 1), y);
            Piece neighbor_piece3 = this.board.get_piece((x + 1), (y + 1));
            piece.r_side = "free";
            neighbor_piece1.r_side = "free";
            neighbor_piece2.l_side = "free";
            neighbor_piece3.l_side = "free";
            this.board.paired_block_pieces.remove(piece, neighbor_piece1);
        }
    }

    public boolean is_winner(){
        String player_pos = this.get_position();
        int x = Integer.parseInt(player_pos.split(",")[0]);
        int y = Integer.parseInt(player_pos.split(",")[1]);
        Piece player_piece = this.board.get_piece(x, y);

        if (this.color.equals("white")){
            if (this.board.get_white_goal_pieces().contains(player_piece)){
                return true;
            }
        }

        if (this.color.equals("black")){
            if (this.board.get_black_goal_pieces().contains(player_piece)){
                return true;
            }
        }

        return false;
    }

    public boolean can_place_wall(Piece piece, String orientation){
        if (this.walls_count > 0){
            String pos = piece.get_position();
            int x = Integer.parseInt(pos.split(",")[0]);
            int y = Integer.parseInt(pos.split(",")[1]);
            if (!piece.is_border_piece){
                if (orientation.equals("horizontal")){
                    if (piece.d_side.equals("free") &&
                            this.board.get_piece((x + 1), y).d_side.equals("free")){
                        if (!this.board.paired_block_pieces.containsKey(piece) &&
                            !this.board.paired_block_pieces.containsValue(this.board.get_piece(x, (y + 1)))){
                                return true;
                        }
                    }
                }
                if (orientation.equals("vertical")){
                    if (piece.r_side.equals("free") &&
                            this.board.get_piece(x, (y + 1)).r_side.equals("free")){
                        if (!this.board.paired_block_pieces.containsKey(piece) &&
                                !this.board.paired_block_pieces.containsValue(this.board.get_piece((x + 1), y))){
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public Set<String> get_legal_actions(Player opponent){
        Piece player_piece = this.board.get_piece(this.x, this.y);
        Piece opponent_piece = this.board.get_piece(opponent.x, opponent.y);

        Set<String> legal_moves = new HashSet<String>();

        if (!player_piece.r_side.equals("block")){
            if (!opponent_piece.get_position().equals((this.x + 1) + "," + this.y)){
                legal_moves.add("move#" + (this.x + 1) + "#" + this.y);
            }
            else {
                if (opponent_piece.r_side.equals("free")){
                    legal_moves.add("move#" + (this.x + 2) + "#" + this.y);
                }
                else {
                    if (opponent_piece.u_side.equals("free")){
                        legal_moves.add("move#" + (this.x + 1) + "#" + (this.y - 1));
                    }
                    if (opponent_piece.d_side.equals("free")){
                        legal_moves.add("move#" + (this.x + 1) + "#" + (this.y + 1));
                    }
                }
            }
        }


        if (!player_piece.d_side.equals("block")){
            if (!opponent_piece.get_position().equals((this.x) + "," + (this.y + 1))){
                legal_moves.add("move#" + (this.x) + "#" + (this.y + 1));
            }
            else {
                if (opponent_piece.d_side.equals("free")){
                    legal_moves.add("move#" + (this.x) + "#" + (this.y + 2));
                }
                else {
                    if (opponent_piece.r_side.equals("free")){
                        legal_moves.add("move#" + (this.x + 1) + "#" + (this.y + 1));
                    }
                    if (opponent_piece.l_side.equals("free")){
                        legal_moves.add("move#" + (this.x - 1) + "#" + (this.y + 1));
                    }
                }
            }
        }


        if (!player_piece.l_side.equals("block")){
            if (!opponent_piece.get_position().equals((this.x - 1) + "," + (this.y))){
                legal_moves.add("move#" + (this.x - 1) + "#" + this.y);
            }
            else {
                if (opponent_piece.l_side.equals("free")){
                    legal_moves.add("move#" + (this.x - 2) + "#" + (this.y));
                }
                else {
                    if (opponent_piece.u_side.equals("free")){
                        legal_moves.add("move#" + (this.x - 1) + "#" + (this.y - 1));
                    }
                    if (opponent_piece.d_side.equals("free")){
                        legal_moves.add("move#" + (this.x - 1) + "#" + (this.y + 1));
                    }
                }
            }
        }


        if (!player_piece.u_side.equals("block")){
            if (!opponent_piece.get_position().equals((this.x) + "," + (this.y - 1))){
                legal_moves.add("move#" + (this.x) + "#" + (this.y - 1));
            }
            else {
                if (opponent_piece.u_side.equals("free")){
                    legal_moves.add("move#" + (this.x) + "#" + (this.y - 2));
                }
                else {
                    if (opponent_piece.l_side.equals("free")){
                        legal_moves.add("move#" + (this.x - 1) + "#" + (this.y - 1));
                    }
                    if (opponent_piece.r_side.equals("free")){
                        legal_moves.add("move#" + (this.x + 1) + "#" + (this.y - 1));
                    }
                }
            }
        }
        /**
         * get opponent's coordinates
         */
        final int lBound = 1;
        String opp_pos = opponent_piece.get_position();
        int oppX = Integer.parseInt(opp_pos.split(",")[0]);
        int oppY = Integer.parseInt(opp_pos.split(",")[1]);
        Set<String> orientation = new HashSet<String>();
        orientation.add("vertical");
        orientation.add("horizontal");
        for (int y = 0; y < this.board.ROWS_NUM; y++) {
            for (int x = 0; x < this.board.COLS_NUM; x++) {
                for (String or : orientation) {
                    if (this.can_place_wall(this.board.boardMap[y][x], or)){
                        String command = "wall#" + this.board.boardMap[y][x].x + "#" + this.board.boardMap[y][x].y + "#" + or;
                        this.put_wall(this.board.boardMap[y][x].x, this.board.boardMap[y][x].y, or);
                        if (this.board.is_reachable(opponent) && this.board.is_reachable(this)){
                            /**
                             * we set a limitation on wall placement. it is bounded to just 3 squares from each side
                             */
                            if ( (Math.abs(oppX-x) <= lBound)  && (Math.abs(oppY-y) <= lBound) ) {
                                legal_moves.add(command);
                            }
                        }
                        this.remove_wall(command);
                    }
                }
            }
        }
        return legal_moves;
    }

}
