package utility;

import ij.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Date;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/*
 * Classe per la creazione di file HTML di report che specifichi le operazioni
 * forensi effettuate su un'immagine.
 * 
 * Per informazioni sull'utilizzo vedere i commenti di ogni metodo oppure il metodo main
 * 
 */
/**
 *
 * @author Tommaso Testa, Salvatore Adriano Zappala', Salvo Scalia
 */
public class HtmlReport {

    private String title = "ImageJ Report";         // intestazione del file html
    private String operation;                       // nome dell'operazione effettuata
    private String summary;                         // breve spiegazione dell'operazione
    private String details;                         // dettagli dell'operazione
    private LinkedList<String> parameters = new LinkedList<String>();           // parametri dell'algoritmo
    private LinkedList<String> parametersDetails = new LinkedList<String>();    // spiegazione di ogni parametro
    private LinkedList<String> parametersValues = new LinkedList<String>();     // valori dei parametri
    private LinkedList<String> references = new LinkedList<String>();           // riferimenti bibliografici
    private String reportDate;                      // data di creazione del report
    private String user;                            // creatore del report
    private String workstation;                     // nome del computer
    private String path;                            // percorso del file (per calcolare l'hash)
    private String md5;                             // impronta md5 del file
    private String sha1;                            // impronta sha-1 del file

    /*
     * Creazione di un nuovo report, come parametro passare il
     * percorso del file immagine.
     */
    public HtmlReport(String path) {
        this.path = path;
        user = System.getProperty("user.name");
        try {
            workstation = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            IJ.showMessage("Impossibile ottenere il nome del computer. Errore: " + ex.getMessage());
            workstation = "Sconosciuto";
        }
    }

    /*
     * Imposta il titolo dell'algoritmo effettuato
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /*
     * Imposta la spiegazione dell'algoritmo effettuato
     */
    public void setDetails(String details) {
        this.details = details;
    }

    /*
     * Imposta un breve riassunto dell'algoritmo effettuato
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /*
     * Aggiunge un nuovo parametro.
     * parameter: nome del parametro da aggiungere
     * parameterDetail: spiegazione del parametro
     * parameterValue: valore del parametro inserito
     */
    public void addParameter(String parameter, String parameterDetail, String parameterValue) {
        parameters.add(parameter);
        parametersDetails.add(parameterDetail);
        parametersValues.add(parameterValue);
    }

    /*
     * Aggiunge un nuovo riferimento bibliografico.
     */
    public void addReference(String reference) {
        references.add(reference);
    }

    /*
     * Metodo che scrive il file di report.
     * filename: nome del file da salvare
     */
    public void generateReport(String filename) {
        PrintWriter writer = null;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(filename));
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "File HTML", "html");
        chooser.setFileFilter(filter);
        chooser.setMultiSelectionEnabled(false);
        int returnVal = chooser.showSaveDialog(null);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        filename = chooser.getSelectedFile().getAbsolutePath();
        try {
            writer = new PrintWriter(new FileOutputStream(filename));
        } catch (FileNotFoundException ex) {
            IJ.showMessage("Impossibile creare il file. Errore: " + ex.getMessage());
        }
        // calcola la data e l'ora attuali
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        reportDate = dateFormat.format(now);
        // calcola le impronte hash
        try {
            md5 = hash(path, "MD5");
            sha1 = hash(path, "SHA-1");
        } catch (Exception ex) {
            IJ.showMessage(ex.getMessage());
        }
        /*
         * Costruisco il file HTML
         */
        // intestazione
        writer.println(""
                + "<html>\n"
                + "<head>\n"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">\n"
                + "</head>");
        ///////// BODY
        // Informazioni generali
        writer.printf(""
                + "<body>\n"
                + "<h1>%s</h1>\n"
                + "Report generation: %s<br />\n"
                + "User: %s<br />\n"
                + "Workstation: %s<br /><br />\n"
                + "File immagine: %s<br />\n"
                + "Impronta hash MD5: %s<br />\n"
                + "Impronta hash SHA-1: %s<br />\n",
                title, reportDate, user, workstation, path, md5, sha1);
        // Informazioni sull'algoritmo
        writer.printf("<hr /><h2>%s</h2>\n"
                + "<i>%s</i><br />\n"
                + "<h3>Dettagli</h3>\n"
                + "%s<br />\n",
                operation, summary, details);
        // Parametri inseriti
        if (!parameters.isEmpty()) {
            writer.println("<h3>Parametri</h3>");
            int i = 0;
            for (String parameter : parameters) {
                String parameterValue = parametersValues.get(i);
                writer.printf("<h4>%s: %s</h4>\n", parameter, parameterValue);
                String parameterDetail = parametersDetails.get(i);
                writer.printf("<i>%s</i><br />\n", parameterDetail);
                i++;
            }
        }
        // Riferimenti bibliografici
        if (!references.isEmpty()) {
            writer.println("<hr /><h2>Riferimenti</h2>"
                    + "<ul>\n");
            for (String reference : references) {
                writer.printf("<li>%s</li>\n", reference);
            }
            writer.println("</ul>\n");
        }
        // footer
        writer.println("<br />\n"
                + "</body>\n"
                + "</html>\n");
        // chiude lo stream
        if (writer != null) {
            writer.close();
        }
        // apre il file creato
        openFile(filename);
    }

    /*
     * Metodo per il calcolo dell'impronta hash di un file.
     * bisogna passare anche l'algoritmo da utilizzare
     * esempio : MD5, SHA-1.
     */
    public static String hash(String filename, String algorithm)
            throws IOException, NoSuchAlgorithmException {
        // apre il file di cui verrà calcolato l'hash
        BufferedInputStream stream = new BufferedInputStream(
                new FileInputStream(filename));
        MessageDigest messageDigest = null;
        messageDigest = MessageDigest.getInstance(algorithm);
        DigestInputStream digestInput = new DigestInputStream(stream, messageDigest);
        while (digestInput.read() != -1); // ciclo vuoto che legge tutto il file       
        // calcola l'hash
        byte[] digest = messageDigest.digest();
        StringBuilder hexString = new StringBuilder();
        // converte i byte in stringa
        for (byte value : digest) {
            hexString.append(Integer.toHexString(0xFF & value));
        }
        // chiude gli stream
        if (digestInput != null) {
            digestInput.close();
        }
        if (stream != null) {
            stream.close();
        }
        return hexString.toString();
    }

    /*
     * Apre un file (utilizzato per aprire gli url generati)
     */
    public static void openFile(String path) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(new File(path));
            } catch (IOException ex) {
                IJ.showMessage("Impossibile aprire il file. Errore: " + ex.getMessage());
            }
        } else {
            IJ.showMessage("Desktop non supportato");
        }
    }

    /*
    public static void main(String[] args) {
        // crea un report per il file immagine et.jpg
        HtmlReport report = new HtmlReport("et.jpg");
        // inserisce il titolo dell'algoritmo effettuato
        report.setOperation("Filtro");
        // inserisce un riassunto di cosa fa il filtro
        report.setSummary("Piccolo riassunto.");
        // inserisce i dettagli di cosa fa il filtro
        report.setDetails("Spiegazione nei dettagli del funzionamento del filtro");
        // inserisce i parametri: nome parametro, dettagli parametro, valore parametro
        report.addParameter("Parametro 1", "Dettagli parametro 1", "5");
        report.addParameter("Parametro 2", "Dettagli parametro 2", "5");
        // inserisce i riferimenti bibliografici
        report.addReference("Mario rossi, Testo di prova, 1969");
        // genera il file di report
        report.generateReport("Prova.html");
    }
     */
}
