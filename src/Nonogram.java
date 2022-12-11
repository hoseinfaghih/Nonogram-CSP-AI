import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

public class Nonogram {

    private State state;
    private int n;
    ArrayList<ArrayList<Integer>> row_constraints;
    ArrayList<ArrayList<Integer>> col_constraints;
    ArrayList <Integer> sum_row_constraint =  new ArrayList<Integer>();
    ArrayList <Integer> sum_col_constraint =  new ArrayList<Integer>();

    public Nonogram(State state,
                    ArrayList<ArrayList<Integer>> row_constraints,
                    ArrayList<ArrayList<Integer>> col_constraints) {
        this.state = state;
        this.n = state.getN();
        this.row_constraints = row_constraints;
        this.col_constraints = col_constraints;
        for (int i=0;i<n;i++){
            int sum = 0;
            for (int j : row_constraints.get(i)){
                sum += j;
            }
            this.sum_row_constraint.add(sum);
        }
        for (int i=0;i<n;i++){
            int sum = 0;
            for (int j : col_constraints.get(i)){
                sum += j;
            }
            this.sum_col_constraint.add(sum);
        }
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

        ac3(mrvRes[0],mrvRes[1],state);

//        System.out.println(mrvRes[0] + " " + mrvRes[1]);
//        state.printBoard();

        for (String s : LCV(state, mrvRes)) {
            State newState = state.copy();

            newState.setIndexBoard(mrvRes[0], mrvRes[1], s);
            newState.removeIndexDomain(mrvRes[0], mrvRes[1], s);

            ForwardChecking(mrvRes[0],mrvRes[1], state);

            if (!isConsistent(newState)) {
                //newState.removeIndexDomain(mrvRes[0], mrvRes[1], s);
                continue;
            }

            if (backtrack(newState)) {
                return true;
            }
        }
        return false;
    }

    public void ForwardChecking(int x,int y, State state){

        ArrayList<Integer> row_constraint = row_constraints.get(x);
        ArrayList<Integer> col_constraint = col_constraints.get(y);

        // row forward-check
        boolean flag = true;
        int count;
        int index = 0;

        for (int i : row_constraint) {
            count = 0;
            int j;
            for(j = index;j < n; j++){
                if(count > 0 && ( state.getBoard().get(x).get(j).equals("X") || state.getBoard().get(x).get(j).equals("E"))){
                    break;
                }
                if(state.getBoard().get(x).get(j).equals("F"))
                    count++;
            }
            index = j;
            if(count != i) {
                flag = false;
                break;
            }

        }
        if(flag){
            for(int j=0;j<n;j++){
                if(state.getBoard().get(x).get(j).equals("E")){
                    state.setIndexBoard(x, j, "X");
                    state.removeIndexDomain(x, j, "F");
                }
            }
        }

        // column forward check

        index = 0;
        flag = true;

        for(int i : col_constraint){
            count = 0;
            int k;
            for(k=index;k<n;k++){
                if (count > 0 && ( state.getBoard().get(k).get(y).equals("X") || state.getBoard().get(k).get(y).equals("E") ) ) {
                    break;
                }
                if(state.getBoard().get(k).get(y).equals("F"))
                    count++;

            }
            index = k;
            if(count != i) {
                flag = false;
                break;
            }
        }

        if(flag){
            for(int i=0;i<state.getN();i++){
                if(state.getBoard().get(i).get(y).equals("E")){
                    state.setIndexBoard(i, y, "X");
                    state.removeIndexDomain(i, y, "F");
                }
            }
        }

    }
    private void ac3(int x, int y , State state) {
        if (row_constraints.get(x).size() == 1) {
            int FirstofRow = row_constraints.get(x).get(0);
            if ((FirstofRow * 2) > n) {
                for (int i = n - FirstofRow; i < row_constraints.get(x).get(0); i++) {
                    state.removeIndexDomain(x, i, "X");
                    state.setIndexBoard(x, i, "F");
                }
            }
        }
        if (col_constraints.get(y).size() == 1) {
            int FirstOfCol = col_constraints.get(y).get(0);
            if ((FirstOfCol * 2) > n) {
                for (int i = n - FirstOfCol; i < FirstOfCol; i++) {
                    state.removeIndexDomain(i, y, "X");
                    state.setIndexBoard(i, y, "F");
                }
            }
        }
        //check first of row
        
        int FirstRowConst = row_constraints.get(x).get(0);
        int cnt = 0, maxBlock = 0;
        boolean F_come = false;
        for (int i = 0; i < y; i++) {
            String a = state.getBoard().get(x).get(i);
            if (a == "F") {
                cnt++;
                F_come = true;
            }
            if (a == "E") {
                cnt++;
            }
            if (a == "X") {
                cnt = 0;
            }
            if (maxBlock < cnt){
                maxBlock = cnt;
            }
        }
        if (F_come == true && maxBlock < FirstRowConst) {
            state.removeIndexDomain(x, y, "X");
            state.setIndexBoard(x, y, "F");
        }
        //Check first of Col

        int FirstColConst = col_constraints.get(y).get(0);
        cnt = 0; maxBlock = 0;
        F_come = false;
        for (int i = 0; i < x; i++) {
            String a = state.getBoard().get(i).get(y);
            if (a == "F") {
                cnt++;
                F_come = true;
            }
            if (a == "E") {
                cnt++;
            }
            if (a == "X") {
                cnt = 0;
            }
            if (maxBlock < cnt){
                maxBlock = cnt;
            }
        }
        if (F_come == true && maxBlock < FirstColConst) {
            state.removeIndexDomain(x, y, "X");
            state.setIndexBoard(x, y, "F");
        }
        // Check Last of Row

        int LastRowConst = row_constraints.get(x).get(row_constraints.get(x).size()-1);
        cnt = 0; maxBlock = 0;
        F_come = false;
        for (int i = n-1; i > y; i--) {
            String a = state.getBoard().get(x).get(i);
            if (a == "F") {
                cnt++;
                F_come = true;
            }
            if (a == "E") {
                cnt++;
            }
            if (a == "X") {
                cnt = 0;
            }
            if (maxBlock < cnt){
                maxBlock = cnt;
            }
        }
        if (F_come == true && maxBlock < LastRowConst) {
            state.removeIndexDomain(x, y, "X");
            state.setIndexBoard(x, y, "F");
        }
        // Check Last of Col

        int LastColConst = col_constraints.get(y).get(col_constraints.get(y).size()-1);
        cnt = 0; maxBlock = 0;
        F_come = false;
        for (int i = n-1; i > x; i--) {
            String a = state.getBoard().get(i).get(y);
            if (a == "F") {
                cnt++;
                F_come = true;
            }
            if (a == "E") {
                cnt++;
            }
            if (a == "X") {
                cnt = 0;
            }
            if (maxBlock < cnt){
                maxBlock = cnt;
            }
        }
        if (F_come == true && maxBlock < LastColConst) {
            state.removeIndexDomain(x, y, "X");
            state.setIndexBoard(x, y, "F");
        }
}

    private ArrayList<String> LCV (State state, int[] var) {
        if (state.getDomain().get(var[0]).get(var[1]).size() < 2){
            return state.getDomain().get(var[0]).get(var[1]);
        }
//        ArrayList<Integer> row_constraint = row_constraints.get(var[0]);
//        ArrayList<Integer> col_constraint = col_constraints.get(var[1]);
        double count_f = 0 , count_e = -1, count_x = 0 , count_const = 0  ;
//        for (int i : row_constraint){
//            count_const += i;
//        }
//        for (int i : col_constraint){
//            count_const += i;
//        }
        count_const += sum_row_constraint.get(var[0]);
        count_const += sum_col_constraint.get(var[1]);
        for (int i = 0 ; i < n ;i++){
            String s1  = state.getBoard().get(var[0]).get(i);
            String s2 = state.getBoard().get(i).get(var[1]);
            switch (s1){
                case "X" : count_x++; break;
                case "F" : count_f++; break;
                default:   count_e++; break;
            }
            switch (s2){
                case "X" : count_x++; break;
                case "F" : count_f++; break;
                default:   count_e++; break;
            }
        }
        ArrayList<String> res = new ArrayList<>();
        if (count_const - count_f > count_e / 4){
            res.add("F");res.add("X");
            return res;
        }
        else{
            res.add("X");res.add("F");
            return res;
        }

    }

    private int[] MRV (State state) {
        ArrayList<ArrayList<String>> cBoard = state.getBoard();
        ArrayList<ArrayList<ArrayList<String>>> cDomain = state.getDomain();

        int min = Integer.MAX_VALUE;

        int[] result = new int[2];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (cBoard.get(i).get(j).equals("E")) {
                    int val = cDomain.get(i).get(j).size();
                    if (val < min) {
                        min = val;
                        result[0] = i;
                        result[1] = j;
                    }
                }
            }
        }
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
                }
                else if (cBoard.get(i).get(j).equals("E")){
                    break;
                }
                else if (cBoard.get(i).get(j).equals("X")) {
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
                }
                else if (cBoard.get(i).get(j).equals("E")){
                    break;
                }
                else if (cBoard.get(i).get(j).equals("X")) {
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