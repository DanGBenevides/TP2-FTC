import java.io.BufferedWriter;
import java.io.FileWriter;

public class AutomatoParser {
    String automatoString;
    Automato automato;

    AutomatoParser(String automatoString) {
        this.automatoString = automatoString;
    }

    public void writeXmlFile(String path) throws Exception{
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(automatoString);
        writer.close();
        System.out.println("Automato escrito com sucesso!: " + path + "\n");
    }
}
