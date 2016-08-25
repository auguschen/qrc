package org.darktech.iot.qrcoder;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

@WebServlet("/g")
public class QREncoderZX extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1637833365548140376L;

	
    public static BufferedImage toBufferedImage(BitMatrix matrix){    
        int width = matrix.getWidth();    
        int height = matrix.getHeight();    
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);    
            
        for(int x=0;x<width;x++){    
            for(int y=0;y<height;y++){    
                image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);    
            }    
        }    
        return image;       
    }  
    
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");
		resp.setCharacterEncoding("utf-8");
		resp.setContentType("image/jpeg");
		String code = req.getParameter("u");
		if (code==null || code.isEmpty()){
			code = req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+req.getContextPath();
		}
		byte[] d = code.getBytes("ISO-8859-1");
		
		String logourl = req.getParameter("l");	
		if (logourl==null || logourl.isEmpty()){
			logourl = req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+req.getContextPath()+"/logo.jpg";
		}
		byte[] logo = logourl.getBytes("ISO-8859-1");
		
		String level = req.getParameter("e");
		if (level==null || level.isEmpty()){
			level = "M";
		}
		Integer width = 200;
		try {
			width = Integer.parseInt(req.getParameter("w"));
		} catch (Exception e){
		}
		
		Integer height = 200;
		try {
			height = Integer.parseInt(req.getParameter("h"));
		} catch (Exception e){
		}
		
		@SuppressWarnings("rawtypes")
		Map<EncodeHintType, Comparable> hints = new HashMap<EncodeHintType, Comparable>();

		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.valueOf(level.toUpperCase()));
		hints.put(EncodeHintType.MARGIN, 1);

		QRCodeWriter writer = new QRCodeWriter();
	    if (d.length > 0) {
			try {
			    BitMatrix bitMatrix = writer.encode(new String(d,"utf-8"), BarcodeFormat.QR_CODE, width, height, hints);
//			    BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
			    BufferedImage image = toBufferedImage(bitMatrix);
	            Graphics2D g = image.createGraphics();
	            
				/**
	             * 读取Logo图片
	             */	            
	            BufferedImage logoimg = ImageIO.read(new URL(new String(logo,"utf-8")));

	            int ratioWidth = image.getWidth()*2/10;  
	            int ratioHeight = image.getHeight()*2/10;
	            
	            LogoConfig logoConfig = new LogoConfig();
	            int widthLogo = logoimg.getWidth()>ratioWidth?ratioWidth:logoimg.getWidth();
	            int heightLogo = logoimg.getHeight()>ratioHeight?ratioHeight:logoimg.getHeight();
	            
	            // 计算图片放置位置
	            int x = (image.getWidth() - widthLogo) / 2;
	            int y = (image.getHeight() - heightLogo) / 2;

	            //开始绘制图片
	            g.drawImage(logoimg, x, y, widthLogo, heightLogo, null);
	            g.setStroke(new BasicStroke(logoConfig.getBorder()));
	            g.setColor(logoConfig.getBorderColor());
	            g.drawRect(x, y, widthLogo, heightLogo);
	            g.dispose();
	             
	            ImageIO.write(image, "jpeg", resp.getOutputStream());
			} catch (WriterException e) {
				e.printStackTrace();
			}
			resp.getOutputStream().flush();
			resp.getOutputStream().close();
			resp.flushBuffer();
	    }

	}

	@Override
	public void init() throws ServletException {
		super.init();
	}

}
