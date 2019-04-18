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
        //����������汾
        WebClient wc = new WebClient(BrowserVersion.CHROME);
        //�Ƿ�ʹ�ò���ȫ��SSL
        wc.getOptions().setUseInsecureSSL(true);
        //����JS��������Ĭ��Ϊtrue
        wc.getOptions().setJavaScriptEnabled(false);
        //����CSS
        wc.getOptions().setCssEnabled(false);
        //js���д���ʱ���Ƿ��׳��쳣
        wc.getOptions().setThrowExceptionOnScriptError(false);
        //״̬�����ʱ���Ƿ��׳��쳣
        wc.getOptions().setThrowExceptionOnFailingStatusCode(true);
        //�Ƿ�����ʹ��ActiveX
        wc.getOptions().setActiveXNative(false);
        //�ȴ�jsʱ��
        wc.waitForBackgroundJavaScript(600*1000);
        //����Ajax�첽���������������Ajax֧��
        wc.setAjaxController(new NicelyResynchronizingAjaxController());
        //���ó�ʱʱ��
        wc.getOptions().setTimeout(waitTime);
        //������ץȡ
        wc.getOptions().setDoNotTrackEnabled(false);
        Boolean flag = true;
        Connection conn=DbUtil.getConnection();//��ȡ���ݿ�����  
        //sqlִ��������  
        PreparedStatement ps=null;  
        //���������  
        ResultSet rs=null;//��ѯ�����������ȷŵ�rs��  
        int i = 1;
    	int j = 1;
        while(flag){
	        try {
	            //ģ���������һ��Ŀ����ַ
	        	String url = "http://search.chinatelecom.com.cn/was5/web/search?page="+i+"&channelid=209604&StringEncoding=utf-8&perpage=30&outlinepage=10";
	            HtmlPage htmlPage = wc.getPage(url);
	            //Ϊ�˻�ȡjsִ�е����� �߳̿�ʼ��˯�ȴ�
	            Thread.sleep(10000);//����̵߳ĵȴ� ��Ϊjs������Ҫʱ���
	            //���ݲ�ͬ��վ����ҳ��
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
		            			   System.out.println("ҳ���ַ��"+href);
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
	                                   System.out.println("���:"+j+"\nҳ���ַ:"+href+"...����:"+title+"����:"+content);
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
				                                   System.out.println("���:"+j+"\nҳ���ַ:"+href+"...����:"+title+"����:"+content);
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
	            //��xml��ʽ��ȡ��Ӧ�ı�
//	            String xml = htmlPage.asXml();
	            //��תΪDocument����return
//	            return Jsoup.parse(xml);
	            //System.out.println(xml.contains("���.xls"));//false
	        } catch (Exception e) {
	            System.out.println("����ҳ���쳣:"+e.toString());
	        }
	        i++;
        }
        DbUtil.close(conn);
       
        DbUtil.close(rs);
    }
	
//	public static void getHtmlPage(int waitTime) {
//        //����������汾
//        WebClient wc = new WebClient(BrowserVersion.CHROME);
//        //�Ƿ�ʹ�ò���ȫ��SSL
//        wc.getOptions().setUseInsecureSSL(true);
//        //����JS��������Ĭ��Ϊtrue
//        wc.getOptions().setJavaScriptEnabled(false);
//        //����CSS
//        wc.getOptions().setCssEnabled(false);
//        //js���д���ʱ���Ƿ��׳��쳣
//        wc.getOptions().setThrowExceptionOnScriptError(false);
//        //״̬�����ʱ���Ƿ��׳��쳣
//        wc.getOptions().setThrowExceptionOnFailingStatusCode(true);
//        //�Ƿ�����ʹ��ActiveX
//        wc.getOptions().setActiveXNative(false);
//        //�ȴ�jsʱ��
//        wc.waitForBackgroundJavaScript(600*1000);
//        //����Ajax�첽���������������Ajax֧��
//        wc.setAjaxController(new NicelyResynchronizingAjaxController());
//        //���ó�ʱʱ��
//        wc.getOptions().setTimeout(waitTime);
//        //������ץȡ
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
//               System.out.println("...����:"+title+"����:"+content);
//            }
//        } catch (Exception e) {
//            System.out.println("����ҳ���쳣:"+e.toString());
//        }
//    }

	
  public static void main(String[] args) {
	  getHtmlPage(10000);
  }
}
