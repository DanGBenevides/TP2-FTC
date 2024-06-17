import java.util.ArrayList;
import java.util.Arrays;

public class Automato {
    private String[][] matriz;
    private String[] alfabeto;

    Automato(int numEstados, int tamanhoAlfabeto, ArrayList<String> alfabeto) {
        matriz = new String[numEstados][tamanhoAlfabeto + 1];
        this.alfabeto = new String[tamanhoAlfabeto + 1];
        for (int i = 0; i < tamanhoAlfabeto; i++) {
            this.alfabeto[i] = alfabeto.get(i);
        }
    }

    Automato(String[][] matriz, String[] alfabeto) {
        this.matriz = matriz;
        this.alfabeto = alfabeto;
    }

    // Preenche a matriz com os estados, transições e estados iniciais e finais
    public void preencheMatriz(ArrayList<String> estados, int estadoInicial, ArrayList<String> estadosFinais,
            ArrayList<String> transicoes) {
        for (int i = 0; i < estados.size(); i++) {
            for (int j = 0; j < alfabeto.length; j++) {
                matriz[i][j] = "-";
            }
        }

        for (int i = 0; i < estados.size(); i++) {
            if (i == estadoInicial)
                if (estadosFinais.contains(estados.get(i)))
                    matriz[i][0] = "->*" + estados.get(i);
                else
                    matriz[i][0] = "->" + estados.get(i);
            else if (estadosFinais.contains(estados.get(i)))
                matriz[i][0] = "*" + estados.get(i);
            else
                matriz[i][0] = estados.get(i);
        }

        for (String key : transicoes) {
            String[] transicao = key.split(" ");
            int i = Integer.parseInt(transicao[0]);
            int j = 0;
            for (int k = 0; k < alfabeto.length; k++) {
                if (alfabeto[k].equals(transicao[2])) {
                    j = k;
                    matriz[i][j + 1] = transicao[1];
                    break;
                }
            }

        }
    }

    // printa a matriz
    public void printMatriz() {
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < alfabeto.length; j++) {
                System.out.print(matriz[i][j] + " ");
            }
            System.out.println();
        }
    }


    // Retorna o automato em formato de string
    public String automatoToString() {
        String resp = "";
        resp += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><!--Created with JFLAP 6.4.--><structure>&#13;\n\t<type>fa</type>&#13;\n\t<automaton>&#13;\n\t\t<!--The list of states.-->&#13;\n";

        for (int i = 0; i < matriz.length; i++) {
            resp += "\t\t<state id=\"" + i + "\" name=\"" + matriz[i][0].replace("->", "").replace("*", "")
                    + "\">&#13;\n";
            resp += "\t\t\t<x>0</x>&#13;\n\t\t\t<y>0</y>&#13;\n";
            if (matriz[i][0].contains("*"))
                resp += "\t\t\t<final/>&#13;\n";
            if (matriz[i][0].contains("->"))
                resp += "\t\t\t<initial/>&#13;\n";
            resp += "\t\t</state>&#13;\n";
        }
        resp += "\t\t<!--The list of transitions.-->&#13;\n";

        for (int i = 0; i < matriz.length; i++) {
            for (int j = 1; j < matriz[i].length; j++) {
                if (!matriz[i][j].equals("-")) {
                    resp += "\t\t<transition>&#13;\n";
                    resp += "\t\t\t<from>" + i + "</from>&#13;\n";
                    resp += "\t\t\t<to>" + matriz[i][j] + "</to>&#13;\n";
                    resp += "\t\t\t<read>" + alfabeto[j - 1] + "</read>&#13;\n";
                    resp += "\t\t</transition>&#13;\n";
                }
            }
        }

        resp += "\t</automaton>&#13;\n</structure>";
        return resp;
    }

    // Completa o automato
    public void completaAutomato() {
        Boolean incompleto = false;

        // Percorre a matriz e verifica se existe algum estado que não possui transição
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 1; j < matriz[i].length; j++) {
                if (matriz[i][j].equals("-")) {
                    incompleto = true;
                    break;
                }
            }
        }

        if (incompleto) {
            String[][] novaMatriz = new String[matriz.length + 1][alfabeto.length];
            novaMatriz[novaMatriz.length - 1][0] = "qE";
            
            for (int i = 1; i < novaMatriz[0].length; i++) {
                novaMatriz[novaMatriz.length - 1][i] = Integer.toString(novaMatriz.length - 1);
            }

            for (int j = 0; j < matriz.length; j++) {
                for (int k = 0; k < matriz[j].length; k++) {
                    if (matriz[j][k].equals("-")) {
                        novaMatriz[j][k] = Integer.toString(novaMatriz.length - 1);
                    } else {
                        novaMatriz[j][k] = matriz[j][k];
                    }
                }
            }

            this.matriz = novaMatriz;
        }
    }


    // Retorna o índice do estado na matriz
    public int getIndiceEstado(String estado) {
        int resp = -1;
        for (int i = 0; i < matriz.length; i++) {
            if (matriz[i][0].equals(estado)) {
                resp = i;
                break;
            }
        }
        return resp;
    }

    // Retorna o índice do estado na matriz
    public String testaTransicao(String estado, int simbolo) {
        for (int i = 0; i < matriz.length; i++) {
            if (matriz[i][0].equals(estado)) {
                for (int j = 1; j < matriz[i].length; j++) {
                    if (j == simbolo + 1) {
                        return matriz[i][j];
                    }
                }
            }
        }
        return "ERRO";
    }

    // Retorna o nome do estado a partir do índice
    public String getNomeEstado(int estado) {
        return matriz[estado][0];
    }

    // Clona a matriz
    public String[][] cloneMatriz(String[][] matriz) {
        String[][] resp = new String[matriz.length][matriz[0].length];

        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                resp[i][j] = matriz[i][j];
            }
        }

        return resp;
    }

    // Simulador do automato
    public boolean simulaAutomato(String cadeia) {
        cadeia = cadeia.trim();
        int estadoAtual = 0;
        int simbolo = 0;
        String estado = matriz[estadoAtual][0];
        String[] cadeiaArray = cadeia.split("");
       
        if (cadeia.length() == 0) {
            if (matriz[estadoAtual][0].contains("*")) {
                return true;
            } else {
                return false;
            }
        }

        while (simbolo < cadeiaArray.length) {
            String proximoEstado = testaTransicao(estado, Arrays.asList(alfabeto).indexOf(cadeiaArray[simbolo]));
            if (proximoEstado.equals("ERRO")) {
                return false;
            }
            if(proximoEstado.equals("-")) return false;
            estadoAtual = getIndiceEstado(getNomeEstado(Integer.parseInt(proximoEstado)));
            if (estadoAtual == -1) {
                return false;
            }
            estado = matriz[estadoAtual][0];
            simbolo++;
        }

        if (matriz[estadoAtual][0].contains("*")) {
            return true;
        } else {
            return false;
        }
    }
}