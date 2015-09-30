package edu.telemarketer.services.servicesimpls;

import edu.telemarketer.http.requests.Request;
import edu.telemarketer.http.responses.FileResponse;
import edu.telemarketer.http.responses.Response;
import edu.telemarketer.http.Status;
import edu.telemarketer.services.Service;
import edu.telemarketer.services.InService;
import edu.telemarketer.util.PropertiesHelper;


@InService(urlPattern = "^/$")
public class SearchService implements Service {
    @Override
    public Response service(Request request) {

        return new FileResponse(Status.SUCCESS_200, PropertiesHelper.getTemplateFile("index.html"));
    }
}
