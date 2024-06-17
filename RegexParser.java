import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class RegexParser {
    private String regex;
    private int pos;
    static int count;
    HashMap<Integer, Character> numToChar = new HashMap<Integer, Character>();
    ArrayList<ArrayList<Integer>> followpos = new ArrayList<ArrayList<Integer>>();
    ArrayList<Character> alfabeto = new ArrayList<Character>();
    HashMap<String, Integer> estados = new HashMap<String, Integer>();
    ArrayList<String> transicoes = new ArrayList<String>();
    ArrayList<String> estadosFinais = new ArrayList<String>();
    ArrayList<String> estadosArray = new ArrayList<String>();
    

    public RegexParser(String regex) {
        this.regex = regex;
        this.pos = 0;
        count = 1;
    }

    // Método que consome o próximo caractere da regex
    private char nextChar() {
        return pos < regex.length() ? regex.charAt(pos++) : '\0';
    }

    // Método que retorna o próximo caractere da regex sem consumi-lo
    private char peekChar() {
        return pos < regex.length() ? regex.charAt(pos) : '\0';
    }

    // Método que faz o parsing da regex e retorna a raiz da árvore sintática
    public RegexNode parse() {
        RegexNode node = parseExpression();
        if (pos != regex.length()) {
            throw new IllegalArgumentException("caracter inesperado ao final do regex: " + peekChar());
        }
        return node;
    }

    // Método que faz o parsing de uma expressão regular
    private RegexNode parseExpression() {
        RegexNode node = parseTerm();
        while (peekChar() == '|') {
            nextChar(); // consome '|'
            RegexNode right = parseTerm();
            node = new UnionNode(node, right);
        }
        return node;
    }

    // Método que faz o parsing de um termo
    private RegexNode parseTerm() {
        RegexNode node = parseFactor();
        while (peekChar() != '\0' && peekChar() != '|' && peekChar() != ')') {
            RegexNode right = parseFactor();
            node = new ConcatNode(node, right);
        }
        return node;
    }

    // Método que faz o parsing de um fator
    private RegexNode parseFactor() {
        RegexNode node = parseBase();
        while (peekChar() == '*') {
            nextChar(); // consome '*'
            node = new StarNode(node);
        }
        return node;
    }

    // Método que faz o parsing de um fator base
    private RegexNode parseBase() {
        if (peekChar() == '(') {
            nextChar(); // consome '('
            RegexNode node = parseExpression();
            if (nextChar() != ')') {
                throw new IllegalArgumentException("parenteses de fechamento fataltando");
            }
            return node;
        } else {
            return new CharNode(nextChar());
        }
    }

    public void initializeFollowPos() {
        for (int i = 0; i < count; i++) {
            followpos.add(new ArrayList<Integer>());
        }
    }

    public void printAlfabeto() {
        System.out.print("Alfabeto: ");
        for (int i = 0; i < alfabeto.size(); i++) {
            System.out.print(alfabeto.get(i) + " ");
        }
        System.out.println();
    }

    public ArrayList<String> getEstadosArray() {
        return estadosArray;
    }
    public ArrayList<String> getEstadosFinais() {
        return estadosFinais;
    }
    public ArrayList<String> getTransicoes() {
        return transicoes;
    }
    public HashMap<String, Integer> getEstados() {
        return estados;
    }
    public ArrayList<ArrayList<Integer>> getFollowPos() {
        return followpos;
    }
    public HashMap<Integer, Character> getNumToChar() {
        return numToChar;
    }
    public ArrayList<String> getAlfabeto() {
        ArrayList<String> resp = new ArrayList<String>();
        for (int i = 0; i < alfabeto.size(); i++) {
            resp.add("" + alfabeto.get(i));
        }
        return resp;
    }


    public void makeAFD(RegexNode node) {
        definePos(node);
        alfabeto.remove(alfabeto.size() - 1);
        initializeFollowPos();
        followPos(node);
        criaTransicoes(node);
        estadosArray = hashMapToArrayList();
        sortEstados();

       
       
    }

    // sort ArrayList<String>
    public void sortEstados() {
        for (int i = 0; i < estadosArray.size(); i++) {
            for (int j = i + 1; j < estadosArray.size(); j++) {
                if (Integer.parseInt(estadosArray.get(i)) > Integer.parseInt(estadosArray.get(j))) {
                    String aux = estadosArray.get(i);
                    estadosArray.set(i, estadosArray.get(j));
                    estadosArray.set(j, aux);
                }
            }
        }
        
    }

    // Método que percorre a árvore sintática e calcula os conjuntos firstpos, lastpos e nullable das folhas
    public void definePos(RegexNode node) {
        if (node instanceof CharNode) {
            CharNode charNode = (CharNode) node;
            charNode.firstpos.add(count);
            charNode.lastpos.add(count);
            numToChar.put(count, charNode.character);
            count++;
            charNode.nullable = false;

            if (!alfabeto.contains(charNode.character)) {
                alfabeto.add(charNode.character);
            }
        } else if (node instanceof ConcatNode) {
            ConcatNode concatNode = (ConcatNode) node;
            definePos(concatNode.left);
            definePos(concatNode.right);
            concatNode.firstpos.addAll(concatNode.left.firstpos);
            if (concatNode.left.nullable) {
                concatNode.firstpos.addAll(concatNode.right.firstpos);
            }
            concatNode.lastpos.addAll(concatNode.right.lastpos);
            if (concatNode.right.nullable) {
                concatNode.lastpos.addAll(concatNode.left.lastpos);
            }
            concatNode.nullable = concatNode.left.nullable && concatNode.right.nullable;
        } else if (node instanceof UnionNode) {
            UnionNode unionNode = (UnionNode) node;
            definePos(unionNode.left);
            definePos(unionNode.right);
            unionNode.firstpos.addAll(unionNode.left.firstpos);
            unionNode.firstpos.addAll(unionNode.right.firstpos);
            unionNode.lastpos.addAll(unionNode.left.lastpos);
            unionNode.lastpos.addAll(unionNode.right.lastpos);
            unionNode.nullable = unionNode.left.nullable || unionNode.right.nullable;
        } else if (node instanceof StarNode) {
            StarNode starNode = (StarNode) node;
            definePos(starNode.child);
            starNode.firstpos.addAll(starNode.child.firstpos);
            starNode.lastpos.addAll(starNode.child.lastpos);
            starNode.nullable = true;
        }
    }

    public void followPos(RegexNode node) {
        if (node instanceof ConcatNode) {
            ConcatNode concatNode = (ConcatNode) node;
            followPos(concatNode.left);
            followPos(concatNode.right);
            for (int i : concatNode.left.lastpos) {
                for (int j : concatNode.right.firstpos) {
                    followpos.get(i).add(j);
                }
            }
        } else if (node instanceof UnionNode) {
            UnionNode unionNode = (UnionNode) node;
            followPos(unionNode.left);
            followPos(unionNode.right);
        } else if (node instanceof StarNode) {
            StarNode starNode = (StarNode) node;
            for (int i : starNode.lastpos) {
                for (int j : starNode.firstpos) {
                    followpos.get(i).add(j);
                }
            }
            followPos(starNode.child);
        }
    }

    public void printFollowPos() {
        for (int i = 1; i < followpos.size(); i++) {
            System.out.print("Followpos(" + i + "): ");
            for (int j = 0; j < followpos.get(i).size(); j++) {
                System.out.print(followpos.get(i).get(j) + " ");
            }
            System.out.println();
        }
    }



    public void criaTransicoes(RegexNode root) {
        ArrayList<ArrayList<Integer>> alfabetoIgual = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> aux = cloneArrayList(root.firstpos);
        String primeiroEstado = posToString(root.firstpos);
        estados.put(primeiroEstado, estados.size());
        Stack<ArrayList<Integer>> stack = new Stack<ArrayList<Integer>>();
        stack.push(aux);
        ArrayList<Integer> aux2 = new ArrayList<Integer>();

        while(!stack.isEmpty()) {
            alfabetoIgual.clear();
            aux2 = cloneArrayList(stack.peek());
            alfabetoIgual = charIgual(stack.pop());
            
            for (int i = 0; i < alfabetoIgual.size(); i++) {
                aux.clear();
                if (alfabetoIgual.get(i).size() > 0) {
                    for (int j = 0; j < alfabetoIgual.get(i).size(); j++) {
                        aux.addAll(followpos.get(alfabetoIgual.get(i).get(j)));
                    }
                    if (!estados.containsKey(posToString(aux))) {
                        estados.put(posToString(aux), estados.size());;
                        stack.push(cloneArrayList(aux));
                    }
                    if(aux.contains(numToChar.size())) {
                        estadosFinais.add("" + estados.get(posToString(aux)));
                    }
                    transicoes.add("" + (estados.get(posToString(aux2))) + " " + estados.get(posToString(aux)) + " " + alfabeto.get(i));
                }
            }
        }
    }

    // hashmap to arraylist
    public ArrayList<String> hashMapToArrayList() {;
        ArrayList<String> aux = new ArrayList<String>();
        for (String key : estados.keySet()) {
            aux.add("" + estados.get(key));
        }
        return aux;
    }


    public ArrayList<Integer> cloneArrayList(ArrayList<Integer> aux) {
        ArrayList<Integer> clone = new ArrayList<Integer>();
        for (int i = 0; i < aux.size(); i++) {
            clone.add(aux.get(i));
        }
        return clone;
    }

    public String posToString(ArrayList<Integer> pos) {
        String aux = "";
        for (int i = 0; i < pos.size(); i++) {
            aux += pos.get(i) + " ";
        }
        return aux;
    }

    public void printEstados() {
        for (String key : estados.keySet()) {
            System.out.println(key + " " + estados.get(key));
        }
    }

    public ArrayList<ArrayList<Integer>> charIgual(ArrayList<Integer> firstpos) {
        ArrayList<ArrayList<Integer>> alfabetoIgual = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < alfabeto.size(); i++) {
            alfabetoIgual.add(new ArrayList<Integer>());
            for (int j = 0; j < firstpos.size(); j++) {
                if (numToChar.get(firstpos.get(j)) == alfabeto.get(i)) {
                    alfabetoIgual.get(i).add(firstpos.get(j));
                }
            }
        }

        return alfabetoIgual;
    }
}

// Classe base abstrata para os nós da árvore sintática
abstract class RegexNode {
    abstract void printTree(String indent);
    ArrayList<Integer> firstpos = new ArrayList<Integer>();
    ArrayList<Integer> lastpos = new ArrayList<Integer>();
    boolean nullable = false;
}

class CharNode extends RegexNode {
    char character;

    CharNode(char character) {
        this.character = character;
    }

    @Override
    void printTree(String indent) {
        System.out.println(indent + "Char: " + character);
    }
}

class ConcatNode extends RegexNode {
    RegexNode left;
    RegexNode right;

    ConcatNode(RegexNode left, RegexNode right) {
        this.left = left;
        this.right = right;
    }

    @Override
    void printTree(String indent) {
        System.out.println(indent + ".");
        left.printTree(indent + "  ");
        right.printTree(indent + "  ");
    }
}

class UnionNode extends RegexNode {
    RegexNode left;
    RegexNode right;

    UnionNode(RegexNode left, RegexNode right) {
        this.left = left;
        this.right = right;
    }

    @Override
    void printTree(String indent) {
        System.out.println(indent + "|");
        left.printTree(indent + "  ");
        right.printTree(indent + "  ");
    }
}

class StarNode extends RegexNode {
    RegexNode child;

    StarNode(RegexNode child) {
        this.child = child;
    }

    @Override
    void printTree(String indent) {
        System.out.println(indent + "*");
        child.printTree(indent + "  ");
    }
}