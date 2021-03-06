package edu.telemarketer.services.impls;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import edu.telemarketer.http.Status;
import edu.telemarketer.http.requests.Request;
import edu.telemarketer.http.requests.MIMEData;
import edu.telemarketer.http.responses.FileResponse;
import edu.telemarketer.http.responses.Response;
import edu.telemarketer.services.InService;
import edu.telemarketer.services.Service;
import edu.telemarketer.util.PropertiesHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Be careful!
 * Created by hason on 15/9/30.
 */
@InService(urlPattern = "^/search$")
public class SearchService implements Service {
    @Override
    public Response service(Request request) {
        if (request.mimeContainKey("photo")) {
            MIMEData photo = request.mimeValue("photo");
            byte[] data = photo.getData();
            try {
                BufferedImage read = ImageIO.read(new ByteInputStream(data, data.length));
                if (read == null) {
                    return new Response(Status.BAD_REQUEST_400);
                }
                showImage(read);
            } catch (IOException e) {
                return new Response(Status.BAD_REQUEST_400);
            }
        } else {
            return new Response(Status.BAD_REQUEST_400);
        }
        return new FileResponse(Status.SUCCESS_200, PropertiesHelper.getTemplateFile("search.html"));
    }

    public static void showImage(BufferedImage image) {
        ImageIcon ic = new ImageIcon(image);
        JFrame world = new JFrame("World");
        JLabel jLabel = new JLabel(ic);
        Panel panel = new Panel();
        panel.add(jLabel);
        world.setContentPane(panel);
        world.setSize(image.getWidth(), image.getHeight());
        world.setVisible(true);
    }

}
