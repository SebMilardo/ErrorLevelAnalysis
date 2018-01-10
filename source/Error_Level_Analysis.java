import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.io.SaveDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.Blitter;
import ij.process.ImageProcessor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import utility.HtmlReport;

/**
 * @author Sebastiano Milardo
 */

public class Error_Level_Analysis implements PlugInFilter {
	ImagePlus imp;
	HtmlReport report;

    @Override
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about")) {
		showAbout();
		return DONE;
		}
                this.imp = imp;
		return DOES_ALL+NO_CHANGES;
	}

    @Override
	public void run(ImageProcessor ip) {
        
		String path = imp.getOriginalFileInfo().directory;
		String directory = path;
		String nomeFile = imp.getTitle();
		path += nomeFile;
				
                GenericDialog dialog = new GenericDialog("Parameters");
                
                dialog.addSlider("Quality", 0, 100, 88);
                dialog.addSlider("Scale", 0, 50, 10);
                dialog.addCheckbox("Generate Report", false);
                
                dialog.showDialog();
                    if (dialog.wasCanceled()) {
                    IJ.error("PlugIn canceled!");
                    return;
                }
		
		int quality = (int)dialog.getNextNumber();
		int scale = (int)dialog.getNextNumber();
		boolean report = dialog.getNextBoolean();
		ImagePlus image = calcErrorLevel(ip,quality,scale);
		image.show();
                image.updateAndDraw();
               
                if (report){// genera il report
                    SaveDialog sdOriginal = new SaveDialog("Save Original Image ...", nomeFile,".jpg");
                    String pathOriginal = sdOriginal.getDirectory();
                    String fileNameOriginal = sdOriginal.getFileName();
                    if (fileNameOriginal==null) return;
                    IJ.save(imp, pathOriginal+fileNameOriginal);

                    SaveDialog sdELA = new SaveDialog("Save Error Level Analysis Image ...", "ErrorLevelAnalysis",".jpg");
                    String pathELA = sdELA.getDirectory();
                    String fileNameELA = sdELA.getFileName();
                    if (fileNameELA==null) return;
                    IJ.save(image, pathELA+fileNameELA);
                    createReport(quality, scale, pathOriginal+fileNameOriginal,pathELA+fileNameELA);
                }       
    }
	
	void showAbout() {
		IJ.showMessage("About Error_Level_Analysis...",
		"Image error level analysis is a technique\n"+
                "that can help to identify manipulations to\n"+
                "compressed (JPEG) images by detecting the \n"+
                "distribution of error introduced after \n"+
                "resaving the image at a specific compression\n"+
                " rate." 
		);
	}
        
        private ImagePlus calcErrorLevel(ImageProcessor ip,int level, int scale){
		int w = ip.getWidth();
		int h = ip.getHeight();
		ImagePlus ImageOriginal = NewImage.createRGBImage("Error Level Analisys", w, h,1, NewImage.FILL_BLACK);
		ImageProcessor original_ip = ImageOriginal.getProcessor();
                original_ip.copyBits(ip,0,0,Blitter.COPY);
                ImagePlus ImageReduced = saveAsJpeg(imp,level);
                ImageProcessor reduced_ip = ImageReduced.getProcessor();
                
                
                int[] pixelsOriginal = (int[]) original_ip.getPixels();
		int[] pixelsReduced = (int[]) reduced_ip.getPixels();
               
                for(int i = 0, l = pixelsOriginal.length; i < l; i++) {
   			int cReduced = pixelsReduced[i];
				
			int rReduced = (cReduced & 0xff0000)>>16;
			int gReduced = (cReduced & 0x00ff00)>>8;
			int bReduced = (cReduced & 0x0000ff);
				
                        int cOriginal = pixelsOriginal[i];
				
                        int rOriginal = (cOriginal & 0xff0000)>>16;
			int gOriginal = (cOriginal & 0x00ff00)>>8;
			int bOriginal = (cOriginal & 0x0000ff);
				
			rReduced=Math.abs(rReduced-rOriginal);
			gReduced=Math.abs(gReduced-gOriginal);
			bReduced=Math.abs(bReduced-bOriginal);
			pixelsReduced[i] = ((rReduced & 0xff) << 16) + ((gReduced & 0xff) << 8) +(bReduced & 0xff);

                        pixelsReduced[i] = pixelsReduced[i]*scale;
                   
                }
                return ImageReduced;
        }  
        
        private ImagePlus saveAsJpeg(ImagePlus imp, int quality) {
            ImagePlus imageScaled = null;
            int width = imp.getWidth();
            int height = imp.getHeight();
            int biType = BufferedImage.TYPE_INT_RGB;
            boolean overlay = imp.getOverlay()!=null && !imp.getHideOverlay();
            if (imp.getProcessor().isDefaultLut() && !imp.isComposite() && !overlay)
                biType = BufferedImage.TYPE_BYTE_GRAY;
            BufferedImage bi = new BufferedImage(width, height, biType);
            String error = null;
            try {
                Graphics g = bi.createGraphics();
                Image img = imp.getImage();
                if (overlay)
                    img = imp.flatten().getImage();
                g.drawImage(img, 0, 0, null);
                g.dispose();            
                Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
                ImageWriter writer = (ImageWriter)iter.next();
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                ImageOutputStream ios = new MemoryCacheImageOutputStream(byteArray);
                writer.setOutput(ios);
                ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality/100f);
                if (quality == 100)
                    param.setSourceSubsampling(1, 1, 0, 0);
                IIOImage iioImage = new IIOImage(bi, null, null);
                writer.write(null, iioImage, param);
                ios.close();
                writer.dispose();

                imageScaled = new ImagePlus("Error Level Analysis", ImageIO.read(new ByteArrayInputStream(byteArray.toByteArray())));

            } catch (Exception e) {
                error = ""+e;
                IJ.error("Jpeg Writer", ""+error);
            }
            return imageScaled;
        }
	
	private void createReport(int quality, int scale, String path1, String path2){
        try {
            report = new HtmlReport(path1);
            
            report.setOperation("Error Level Analysis Plugin");
            
            report.setSummary("Calcola dell'Error Level Analysis"); 
            report.setDetails("\"Error Level Analysis\" genera un'analisi dell'immagine alla ricerca "
                    + "di aree modificate artificialmente, sfruttando la compressione del formato JPEG.");
            
            report.addParameter("Qualita'", "Qualita' per l'immagine JPEG risalvata.", quality+"");
            report.addParameter("Scala", "Scala usata per amplificare le differenze tra immagine originale e risalvata.", scale+"");
            
            report.addParameter("Percorso Immagine Originale", "", path1);
                        
            report.addParameter("HASH MD5", "Hash MD5 dell'immagine originale.", HtmlReport.hash(path1, "MD5"));
            report.addParameter("HASH SHA-1", "Hash SHA-1 dell'immagine originale.", HtmlReport.hash(path1, "SHA-1"));
            
            report.addParameter("Percorso Immagine Error Level Analysis", "", path2);
                        
            report.addParameter("HASH MD5", "Hash MD5 dell'Error Level Analysis.", HtmlReport.hash(path2, "MD5"));
            report.addParameter("HASH SHA-1", "Hash SHA-1 dell'Error Level Analysis.", HtmlReport.hash(path2, "SHA-1"));
            
            report.addReference("Neal Krawetz, A Picture's Worth - Digital Image Analysis and Forensics, Black Hat Briefings, USA 2007."); 
           
            report.generateReport("Report_ELA.html");
            } catch (Exception ex) {
                IJ.showMessage(ex.getMessage());
            }
	}
	
	
}