package edu.telemarketer.http.requests;

import edu.telemarketer.http.requests.mime.MIMEData;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Be careful!
 * Created by hason on 15/9/29.
 */
class RequestBody {

    private static Logger logger = Logger.getLogger("RequestBody");
    private Map<String, String> formMap;
    private Map<String, MIMEData> mimeMap;


    public RequestBody(Map<String, String> formMap, Map<String, MIMEData> mimeMap) {
        this.formMap = formMap;
        this.mimeMap = mimeMap;
    }

    public RequestBody() {
        this.formMap = Collections.emptyMap();
        this.mimeMap = Collections.emptyMap();
    }

    public static RequestBody parseBody(byte[] body, RequestHeader header) {

        String contentType = header.getContentType();


        Map<String, MIMEData> mimeMap = Collections.emptyMap();

        Map<String, String> formMap = new HashMap<>();
        if (contentType.contains("application/x-www-form-urlencoded")) {
            try {
                String bodyMsg = new String(body, "utf-8");
                parseParameters(bodyMsg, formMap);
            } catch (UnsupportedEncodingException e) {
                logger.log(Level.SEVERE, "基本不可能出现的错误 编码方法不支持");
                throw new RuntimeException(e);
            }
        } else if (contentType.contains("multipart/form-data")) {
            int boundaryValueIndex = contentType.indexOf("boundary=");
            String bouStr = contentType.substring(boundaryValueIndex + 9); // 9是 `boundary=` 长度
            mimeMap = parseFormData(body, bouStr);
        }
        return new RequestBody(formMap, mimeMap);
    }

    public static void parseParameters(String s, Map<String, String> requestParameters) {
        String[] paras = s.split("&");
        for (String para : paras) {
            String[] split = para.split("=");
            requestParameters.put(split[0], split[1]);
        }
    }

    /**
     * @param body   body
     * @param bouStr boundary 字符串
     * @return name和mime数据的map
     */
    private static Map<String, MIMEData> parseFormData(byte[] body, String bouStr) {
        Map<String, MIMEData> mimeData = new HashMap<>();

        int bouLength = bouStr.length();
        String tmpBody = new String(body);
        int lastIndex = tmpBody.lastIndexOf(bouStr);
        int startIndex;
        int endIndex = tmpBody.indexOf(bouStr);
        String curBody;

        do {
            //  ------WebKitFormBoundaryIwVsTjLkjugAgonI
            //  Content-Disposition: form-data; name="photo"; filename="15-5.jpeg"
            //  Content-Type: image/jpeg
            //  \r\n
            //  .....
            //  ------WebKitFormBoundaryIwVsTjLkjugAgonI
            //  Content-Disposition: form-data; name="desc"
            //  some words
            //  ------WebKitFormBoundaryIwVsTjLkjugAgonI
            //
            //  请求就是这样,所以会用魔数比较快。response 的话会有别的disposition。
            //  然而这里默认了 name在前 filename在后,不知道会不会不符合规定,若是不能默认那还是用正则吧
            startIndex = endIndex + bouLength;
            endIndex = tmpBody.indexOf(bouStr, startIndex + bouLength);
            curBody = tmpBody.substring(startIndex + 2, endIndex); //去掉\r\n
            int lineEndIndex = curBody.indexOf("\r\n");
            String lineOne = curBody.substring(0, lineEndIndex);
            int leftQuoIndex = lineOne.indexOf('"');
            int rightQuoIndex = lineOne.indexOf('"', leftQuoIndex + 1);
            String name;
            String fileName = null;
            String mimeType = null;
            byte[] data;
            name = lineOne.substring(leftQuoIndex + 1, rightQuoIndex);
            leftQuoIndex = lineOne.indexOf('"', rightQuoIndex + 1);
            int curIndex;
            if (leftQuoIndex != -1) {
                rightQuoIndex = lineOne.indexOf('"', leftQuoIndex + 1);
                fileName = lineOne.substring(leftQuoIndex + 1, rightQuoIndex);
                int headEndIndex = curBody.indexOf("\r\n\r\n", 13);
                mimeType = curBody.substring(lineEndIndex + 13, headEndIndex);
                curIndex = headEndIndex + 4;
            } else {
                curIndex = lineEndIndex + 2;
            }
            data = curBody.substring(curIndex, curBody.length()).getBytes();
            mimeData.put(name, new MIMEData(mimeType, data, fileName));
        } while (endIndex != lastIndex);
        return mimeData;
    }

    public boolean formContainKey(String key) {
        return formMap.containsKey(key);
    }

    public boolean mimeContainKey(String key) {
        return formMap.containsKey(key);
    }


    public Map<String, String> getFormMap() {
        return Collections.unmodifiableMap(formMap);
    }

    public Map<String, MIMEData> getMimeMap() {
        return Collections.unmodifiableMap(mimeMap);
    }

    public String formValue(String key) {
        return formMap.get(key);
    }

    public MIMEData mimeValue(String key) {
        return mimeMap.get(key);
    }
}
