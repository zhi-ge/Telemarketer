package edu.telemarketer.services.impls;

import edu.telemarketer.http.Status;
import edu.telemarketer.http.requests.Request;
import edu.telemarketer.http.responses.FileResponse;
import edu.telemarketer.http.responses.NotFoundResponse;
import edu.telemarketer.http.responses.Response;
import edu.telemarketer.services.Service;
import edu.telemarketer.services.InService;
import edu.telemarketer.util.PropertiesHelper;

import java.io.File;

/**
 * 静态文件服务
 */
@InService(urlPattern = "^" + StaticFileService.prefix + ".*$")
public class StaticFileService implements Service {

    public static final String prefix = "/s/";
    private static String staticPath;

    static {
        staticPath = PropertiesHelper.getProperty("static_path");
    }

    @Override
    public Response service(Request request) {
        String filePath = staticPath + File.separator + request.getURI().replaceAll(prefix, "");
        File file = new File(filePath);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return new NotFoundResponse();
        }
        return new FileResponse(Status.SUCCESS_200, file);
    }
}
