import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class NewsCrawler {
	public static void getHtmlPage(int waitTime) {
        //设置浏览器版本
        WebClient wc = new WebClient(BrowserVersion.CHROME);
        //是否使用不安全的SSL
        wc.getOptions().setUseInsecureSSL(true);
        //启用JS解释器，默认为true
        wc.getOptions().setJavaScriptEnabled(false);
        //禁用CSS
        wc.getOptions().setCssEnabled(false);
        //js运行错误时，是否抛出异常
        wc.getOptions().setThrowExceptionOnScriptError(false);
        //状态码错误时，是否抛出异常
        wc.getOptions().setThrowExceptionOnFailingStatusCode(true);
        //是否允许使用ActiveX
        wc.getOptions().setActiveXNative(false);
        //等待js时间
        wc.waitForBackgroundJavaScript(600*1000);
        //设置Ajax异步处理控制器即启用Ajax支持
        wc.setAjaxController(new NicelyResynchronizingAjaxController());
        //设置超时时间
        wc.getOptions().setTimeout(waitTime);
        //不跟踪抓取
        wc.getOptions().setDoNotTrackEnabled(false);
        Boolean flag = true;
        Connection conn=DbUtil.getConnection();//获取数据库连接  
        //sql执行器对象  
        PreparedStatement ps=null;  
        //结果集对象  
        ResultSet rs=null;//查询出来的数据先放到rs中  
        int i = 1;
    	int j = 1;
        while(flag){
	        try {
	            //模拟浏览器打开一个目标网址
	        	String url = "http://search.chinatelecom.com.cn/was5/web/search?page="+i+"&channelid=209604&StringEncoding=utf-8&perpage=30&outlinepage=10";
	            HtmlPage htmlPage = wc.getPage(url);
	            //为了获取js执行的数据 线程开始沉睡等待
	            Thread.sleep(10000);//这个线程的等待 因为js加载需要时间的
	            //根据不同网站解析页面
	            DomNodeList<DomElement> elementsDiv = htmlPage.getElementsByTagName("ul");
	            for(DomElement domElement:elementsDiv){
	               if("xw_box".equals(domElement.getAttribute("class"))){
	            	   DomNodeList<HtmlElement> elementsH3 = domElement.getElementsByTagName("li");
	            	   if(elementsH3!=null && elementsH3.size()>0){
	            		   for (HtmlElement htmlElement : elementsH3) {
		            		   elementsH3 = htmlElement.getElementsByTagName("a");
		            		   for(HtmlElement htmlElement1 : elementsH3){
		            			   String title = "";
		            			   String content = "";
		            			   String href = htmlElement1.getAttribute("href");
		            			   if(!href.contains("http")||href.contains("www7")){
		            				   continue;
		            			   }
		            			   System.out.println("页面地址："+href);
		            			   htmlPage = wc.getPage(href);
		            			   if(htmlPage.getWebResponse().getStatusCode()!=200){
		            				   continue;
		            			   }
		                           elementsDiv = htmlPage.getElementsByTagName("div");
		                           DomElement p_title = htmlPage.getElementById("p_title");
		                           DomElement p_content = htmlPage.getElementById("p_content");
		                           if(p_title!=null && p_content!=null){
		                        	   title = p_title.asText();
		                        	   content = p_content.asText();
		                        	   content = content.replaceAll(" ", "");
	                                   content = content.replaceAll("\r|\n", "");
	                                   System.out.println("编号:"+j+"\n页面地址:"+href+"...标题:"+title+"内容:"+content);
	                                   String sql = "insert into NEWS (ID,TITLE,CONTENT) VALUES(seq_NEWS.nextval,?,?)";
	                                   ps = (PreparedStatement) conn.prepareStatement(sql);
	                                   ps.setString(1, title);
	                                   ps.setString(2, content);
	                                   ps.executeUpdate();
	                                   DbUtil.close(ps);
		                           }else{
		                        	   if(elementsDiv!=null && elementsDiv.size()>0){
		                        		   for(DomElement domElement1:elementsDiv){
				                        	   if("xw_box xw_box_a".equals(domElement1.getAttribute("class"))){
				                        		   title = domElement1.asText();
				                        	   }
				                        	   if("xw_zj_encryption".equals(domElement1.getAttribute("class"))){
				                        		   content = domElement1.asText();
				                        		   content = content.replaceAll(" ", "");
				                                   content = content.replaceAll("\r|\n", "");
				                                   System.out.println("编号:"+j+"\n页面地址:"+href+"...标题:"+title+"内容:"+content);
				                                   String sql = "insert into NEWS (ID,TITLE,CONTENT) VALUES(seq_NEWS.nextval,?,?)";
				                                   ps = (PreparedStatement) conn.prepareStatement(sql);
				                                   ps.setString(1, title);
				                                   ps.setString(2, content);
				                                   ps.executeUpdate();
				                                   DbUtil.close(ps);
				                        	   }
				                           }
		                        	   }
		                           }
		                           j++;
		            		   }
		            	   }
	            	   }else{
	            		   flag = false;
	            	   }
	               }
	            }
	            //以xml形式获取响应文本
//	            String xml = htmlPage.asXml();
	            //并转为Document对象return
//	            return Jsoup.parse(xml);
	            //System.out.println(xml.contains("结果.xls"));//false
	        } catch (Exception e) {
	            System.out.println("解析页面异常:"+e.toString());
	        }
	        i++;
        }
        DbUtil.close(conn);
       
        DbUtil.close(rs);
    }
	
//	public static void getHtmlPage(int waitTime) {
//        //设置浏览器版本
//        WebClient wc = new WebClient(BrowserVersion.CHROME);
//        //是否使用不安全的SSL
//        wc.getOptions().setUseInsecureSSL(true);
//        //启用JS解释器，默认为true
//        wc.getOptions().setJavaScriptEnabled(false);
//        //禁用CSS
//        wc.getOptions().setCssEnabled(false);
//        //js运行错误时，是否抛出异常
//        wc.getOptions().setThrowExceptionOnScriptError(false);
//        //状态码错误时，是否抛出异常
//        wc.getOptions().setThrowExceptionOnFailingStatusCode(true);
//        //是否允许使用ActiveX
//        wc.getOptions().setActiveXNative(false);
//        //等待js时间
//        wc.waitForBackgroundJavaScript(600*1000);
//        //设置Ajax异步处理控制器即启用Ajax支持
//        wc.setAjaxController(new NicelyResynchronizingAjaxController());
//        //设置超时时间
//        wc.getOptions().setTimeout(waitTime);
//        //不跟踪抓取
//        wc.getOptions().setDoNotTrackEnabled(false);
//        try {
//        	HtmlPage htmlPage = wc.getPage("http://www.chinatelecom.com.cn/tech/hot/rh/nh/zjtrh/200608/t20060810_14219.html");
//        	DomElement p_title = htmlPage.getElementById("p_title");
//            DomElement p_content = htmlPage.getElementById("p_content");
//            String title = "";
//            String content = "";
//            if(p_title!=null && p_content!=null){
//         	   title = p_title.asText();
//         	   content = p_content.asText();
//         	   content = content.replaceAll(" ", "");
//               content = content.replaceAll("\r|\n", "");
//               System.out.println("...标题:"+title+"内容:"+content);
//            }
//        } catch (Exception e) {
//            System.out.println("解析页面异常:"+e.toString());
//        }
//    }

	
  public static void main(String[] args) {
	  getHtmlPage(10000);
  }
}
