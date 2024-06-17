import java.io.BufferedReader;
import java.io.FileReader;

public class Main {
    public static void main(String[] args) throws Exception{
        FileReader arquivoReader = new FileReader("Entrada/ER.jff");
        BufferedReader bufferedReader = new BufferedReader(arquivoReader);    

        String linha = "";
        for (int i = 0; i < 4; i++) {
            linha = bufferedReader.readLine();
        }
        linha = linha.substring(16, linha.length() - 18);
        System.out.println(linha);

        bufferedReader.close();
        String expressao = "";
        expressao += linha + "#";
        expressao = expressao.replace('U', '|');
        expressao = expressao.replace('+', '|');
        System.out.println("Expressao: " + expressao);

        RegexParser parser = new RegexParser(expressao);
        RegexNode syntaxTree = parser.parse();
        parser.makeAFD(syntaxTree);
        
        Automato automato = new Automato(parser.getEstadosArray().size(), parser.getAlfabeto().size(), parser.getAlfabeto());
        automato.preencheMatriz(parser.getEstadosArray(), 0, parser.getEstadosFinais(), parser.getTransicoes());
        String automatoString = automato.automatoToString();
        AutomatoParser automatoParser = new AutomatoParser(automatoString);
        try {
            automatoParser.writeXmlFile("automato.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        arquivoReader = new FileReader("Entrada/sentencas2.txt");
        bufferedReader = new BufferedReader(arquivoReader);
        linha = bufferedReader.readLine();
        while(linha != null) {
            System.out.print("Sentenca: " + linha + " ");
            System.out.print(automato.simulaAutomato(linha)+"\n");   
            linha = bufferedReader.readLine();
        }

        
        bufferedReader.close();
        arquivoReader.close();
    }
}
