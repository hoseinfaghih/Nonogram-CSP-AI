import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

public class Nonogram {

    private State state;
    private int n;
    ArrayList<ArrayList<Integer>> row_constraints;
    ArrayList<ArrayList<Integer>> col_constraints;

    public Nonogram(State state,
                    ArrayList<ArrayList<Integer>> row_constraints,
                    ArrayList<ArrayList<Integer>> col_constraints) {
        this.state = state;
        this.n = state.getN();
        this.row_constraints = row_constraints;
        this.col_constraints = col_constraints;
    }


    public void start() {
        long tStart = System.nanoTime();
        backtrack(state);
        long tEnd = System.nanoTime();
        System.out.println("Total time: " + (tEnd - tStart)/1000000000.000000000);
    }

    private boolean backtrack(State state) {

        if (isFinished(state)) {
            System.out.println("Result Board: \n");
            state.printBoard();
            return true;
        }
        if (allAssigned(state)) {
            return false;
        }

        int[] mrvRes = MRV(state);
        for (String s : LCV(state, mrvRes)) {
            State newState = state.copy();
            newState.setIndexBoard(mrvRes[0], mrvRes[1], s);
            newState.removeIndexDomain(mrvRes[0], mrvRes[1], s);
            //newState.printBoard();
            forward_Checking(newState,mrvRes[0],mrvRes[1]);
            if (!isConsistent(newState)) {
                continue;
            }

            if (backtrack(newState)) {
                return true;
            }
        }
        return false;
    }

    private void forward_Checking (State state,int x,int y) {
        ArrayList<Integer> row_constraint = row_constraints.get(x);
        ArrayList<Integer> col_constraint = col_constraints.get(y);
        int sumcs_row = 0 , sumcs_col = 0 , sumf_row = 0 , sumf_col = 0 , sumx_row = 0 , sumx_col = 0;
        for (int i : row_constraint){
            sumcs_row += i ;
        }
        for (int i : col_constraint){
            sumcs_col += i;
        }
        ArrayList<ArrayList<String>> cBoard = state.getBoard();
        for (int i = 0;i < n ; i++){
            if (cBoard.get(x).get(i).equals("F")){
                sumf_row++;
            }
            if (cBoard.get(x).get(i).equals("X")){
                sumx_row++;
            }
        }
        //System.out.println(x + " " + y + " "  + sumcs_row + " " + sumcs_col );
        if (sumf_row == sumcs_row){
            for (int i=0;i<n;i++){
                if (state.getBoard().get(x).get(i).equals("E")) {
                    state.removeIndexDomain(x, i, "F");
                    state.setIndexBoard(x, i, "X");
                }
            }
        }
        if (sumf_col == sumcs_col){
            for (int i=0;i<n;i++){
                if (state.getBoard().get(x).get(i).equals("E")) {
                    state.removeIndexDomain(i, y, "F");
                    state.setIndexBoard(i, y, "X");
                }
            }
        }

    }

    private ArrayList<String> LCV (State state, int[] var) {
        return state.getDomain().get(var[0]).get(var[1]);
    }

    private int[] MRV (State state) {
        ArrayList<ArrayList<String>> cBoard = state.getBoard();
        ArrayList<ArrayList<ArrayList<String>>> cDomain = state.getDomain();

        int[] result = new int[2];
        int min = 3 , ans_x = 0, ans_y = 0;
        for (int i=0;i<n;i++){
            for(int j=0;j<n;j++){
                if (cDomain.get(i).get(j).size() < min  && cBoard.get(i).get(j).equals("E")){
                    ans_x = i;
                    ans_y = j;
                    min = cDomain.get(i).get(j).size();
                }
            }
        }
        result[0] = ans_x;
        result[1] = ans_y;
        //System.out.println(result[0] + " " + result[1] + " " + min + " " + cBoard.get(0).get(0).equals("E"));
        return result;
    }

    private boolean allAssigned(State state) {
        ArrayList<ArrayList<String>> cBoard = state.getBoard();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                String s = cBoard.get(i).get(j);
                if (s.equals("E"))
                    return false;
            }
        }
        return true;
    }


    private boolean isConsistent(State state) {

        ArrayList<ArrayList<String>> cBoard = state.getBoard();
        //check row constraints
        for (int i = 0; i < n; i++) {
            int sum = 0;
            for (int x : row_constraints.get(i)) {
                sum += x;
            }
            int count_f = 0;
            int count_e = 0;
            int count_x = 0;
            for (int j = 0; j < n; j++) {
                if (cBoard.get(i).get(j).equals("F")) {
                    count_f++;
                } else if (cBoard.get(i).get(j).equals("E")) {
                    count_e++;
                } else if (cBoard.get(i).get(j).equals("X")) {
                    count_x++;
                }
            }

            if (count_x > n - sum) {
                return false;
            }
            if (count_f != sum && count_e == 0) {
                return false;
            }

            Queue<Integer> constraints = new LinkedList<>();
            constraints.addAll(row_constraints.get(i));
            int count = 0;
            boolean flag = false;
            for (int j = 0; j < n; j++) {
                if (cBoard.get(i).get(j).equals("F")) {
                    flag = true;
                    count++;
                } else if (cBoard.get(i).get(j).equals("X")) {
                    if (flag) {
                        flag = false;
                        if (!constraints.isEmpty()){
                            if (count != constraints.peek()) {
                                return false;
                            }
                            constraints.remove();
                        }
                        count = 0;
                    }
                }
            }

        }

        //check col constraints

        for (int j = 0; j < n; j++) {
            int sum = 0;
            for (int x : col_constraints.get(j)) {
                sum += x;
            }
            int count_f = 0;
            int count_e = 0;
            int count_x = 0;
            for (int i = 0; i < n; i++) {
                if (cBoard.get(i).get(j).equals("F")) {
                    count_f++;
                } else if (cBoard.get(i).get(j).equals("E")) {
                    count_e++;
                } else if (cBoard.get(i).get(j).equals("X")) {
                    count_x++;
                }
            }
            if (count_x > n -sum) {
                return false;
            }
            if (count_f != sum && count_e == 0) {
                return false;
            }

            Queue<Integer> constraints = new LinkedList<>();
            constraints.addAll(col_constraints.get(j));
            int count = 0;
            boolean flag = false;
            for (int i = 0; i < n; i++) {
                if (cBoard.get(i).get(j).equals("F")) {
                    flag = true;
                    count++;
                } else if (cBoard.get(i).get(j).equals("X")) {
                    if (flag) {
                        flag = false;
                        if (!constraints.isEmpty()){
                            if (count != constraints.peek()) {
                                return false;
                            }
                            constraints.remove();
                        }
                        count = 0;
                    }
                }
            }
        }
        return true;
    }

    private boolean isFinished(State state) {
        return allAssigned(state) && isConsistent(state);
    }

}