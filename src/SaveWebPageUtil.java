import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName: SaveWebpageTest
 * @CoderName: Eric Wong
 * @Date: 2019/1/12
 * @Desc: SWPU ---> Java离线网页储存解决方案.
 */
public class SaveWebPageUtil {


    /**
     * 目录层级结构为:
     *
     * NAME.html
     * NAME_file
     *    |--srcName.jpg
     *    |--srcName.svg
     *    |--srcName.css
     *    |--srcName.js
     *    |--srcName.png
     */

    //父目录
    public static final String PATH = "/Users/ericwong/swpu/";
    //统一文件夹名称为 NAME;
    public static String NAME = "";

    public static void download(String url,String fileName) throws Exception {
        //初始化文件夹
        initFile(fileName);

        String html = downloadUrl(url);

        List<List> resouceList = getResouceList(html);

        downloadResourceList(resouceList);

        insertData(PATH+NAME+".html",praseHtml(html,resouceList));
//        insertData(PATH+NAME+".html",html);

    }


    private static void initFile(String name) throws Exception {
        File file = new File(PATH);
        if(!file.exists()||!file.isDirectory()){
            throw new Exception("不存在该文件夹!");
        }
        NAME = name;
        file = new File(PATH+NAME+"_file");
        file.mkdir();
    }


    /**
     * @Description
     * 获得初始html文本资源
     * @parameters  [url]
     * @return  java.lang.String
     */
    private static String downloadUrl(String url) throws Exception {

        url = urlVerify(url);
        URLConnection urlConnection = new URL(url).openConnection();
        HttpURLConnection connection = null;
        if (urlConnection instanceof HttpURLConnection) {
            connection = (HttpURLConnection) urlConnection;
        } else {
            throw new Exception("url获取失败!");
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String urlString = "";
        String current;
        while ((current = in.readLine()) != null) {
            urlString += (current + "\n");
        }
        return urlString;
    }

    /**
     * @Description
     * 转换html内img标签网址为本地地址
     * @parameters  [html, rsList]
     * @return  java.lang.String
     */
    private static String praseHtml(String html,List<List> rsList){
        List<String> imgList = rsList.get(0);
        List<String> jsList = rsList.get(1);
        List<String> cssList = rsList.get(2);

        for (int i = 0; i < imgList.size(); i++) {
            if(!imgList.get(i).isEmpty()) {
                String sname = imgList.get(i).substring(imgList.get(i).lastIndexOf("."));
                if(sname.contains("?")) sname = ".png";
                html = html.replace(imgList.get(i),"./" + NAME + "_file" + "/" + i + sname);
            }
        }

        for (int i = 0; i < jsList.size(); i++) {
            if(!jsList.get(i).isEmpty()) {
                html = html.replace(jsList.get(i),"./" + NAME + "_file" + "/" + i + ".js");
            }
        }

        for (int i = 0; i < cssList.size(); i++) {
            if(!cssList.get(i).isEmpty()) {
                html = html.replace(cssList.get(i),"./" + NAME + "_file" + "/" + i + ".css");
            }
        }

        html = html.replace("data-src","src");

        return html;
    }

    /**
     * @Description
     * 下载资源文件
     * @parameters  [rsList]
     * @return  void
     */
    private static void downloadResourceList(List<List> rsList) throws Exception {
        List<String> imgList = rsList.get(0);
        List<String> jsList = rsList.get(1);
        List<String> cssList = rsList.get(2);

        for (int i = 0; i < imgList.size(); i++) {
            if(!imgList.get(i).isEmpty()) {
                String sname = imgList.get(i).substring(imgList.get(i).lastIndexOf("."));
                if(sname.contains("?")||sname.contains("/")||sname.length() > 7) sname = ".png";
                downloadFile(PATH + NAME + "_file" + "/" + i + sname, imgList.get(i));
            } else System.out.println("链接解析失败...");
        }

        for (int i = 0; i < jsList.size(); i++) {
            if(!jsList.get(i).isEmpty()) {
                downloadFile(PATH + NAME + "_file" + "/" + i + ".js", jsList.get(i));
            } else System.out.println("链接解析失败...");
        }

        for (int i = 0; i < cssList.size(); i++) {
            if(!cssList.get(i).isEmpty()) {
                downloadFile(PATH + NAME + "_file" + "/" + i + ".css", cssList.get(i));
            } else System.out.println("链接解析失败...");
        }

    }

    /**
     * @Description
     * 获取资源文件
     * @parameters  [content]
     * @return  java.util.List<java.util.List>
     */
    private static List<List> getResouceList(String content){
        List<List> rsList = new ArrayList<>();
        Document document = Jsoup.parse(content);
        rsList.add(getImgList(document));
        rsList.add(getJsList(document));
        rsList.add(getCssList(document));
        return rsList;
    }

    private static List<String> getImgList(Document document){
        List<String> imgList = new ArrayList<>();
        Elements elements = document.select("img");
        for(Element img:elements){
            String pre_src = img.attr("src");
            if(!imgList.contains(pre_src)){
                imgList.add(pre_src);
            }
        }
        for(Element img:elements){
            String pre_src = img.attr("data-src");
            if(!imgList.contains(pre_src)){
                imgList.add(pre_src);
            }
        }
        System.out.println("imgList:\n"+imgList);
        return imgList;
    }

    private static List<String> getJsList(Document document){
        List<String> jsList = new ArrayList<>();
        Elements elements = document.select("script");
        for(Element img:elements){
            String pre_src = img.attr("src");
            if(!jsList.contains(pre_src)){
                jsList.add(pre_src);
            }
        }
        System.out.println("jsList:\n"+jsList);
        return jsList;
    }

    private static List<String> getCssList(Document document){
        List<String> cssList = new ArrayList<>();
        Elements elements = document.select("link");
        for(Element img:elements){
            //System.out.println(img.attr("href"));
            if(img.attr("rel").equals("stylesheet")) {
                String pre_src = img.attr("href");
                if (!cssList.contains(pre_src)) {
                    cssList.add(pre_src);
                }
            }
        }
        System.out.println("cssList:\n"+cssList);
        return cssList;
    }

    /**
     * @Description
     * 解析url符合格式
     * @parameters  [url]
     * @return  java.lang.String
     */
    private static String urlVerify(String url) {
        if(!url.isEmpty()) {
            if (!url.contains("http")) {
                if (url.startsWith("//")) {
                    url = url.substring(2);
                }
                System.out.println("load...:" + "http://" + url);
                return "http://" + url;
            } else {
                System.out.println("load...:" + url);
                return url;
            }
        }
        return url;
    }

    /**
     * @return java.lang.StringBuilder
     * @Description 储存文件从String
     * @parameters [name]
     */
    private static void insertData(String path, String info) {
        File file = new File(path);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(info);
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return java.lang.StringBuilder
     * @Description 储存文件从url
     * @parameters [name]
     */
    private static void downloadFile(String path, String url) {
        url = urlVerify(url);
        int bytesum = 0;
        int byteread = 0;
        try {
            URL surl = new URL(url);
            URLConnection conn = surl.openConnection();
            InputStream inStream = conn.getInputStream();
            FileOutputStream fs = new FileOutputStream(path);

            byte[] buffer = new byte[1204];
            int length;
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread;
                fs.write(buffer, 0, byteread);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
