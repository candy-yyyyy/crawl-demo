import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


public class LuceneIndex {
	 /**  
     * ��������  
     * @param analyzer  
     * @throws Exception  
     */  
    public static void createIndex(Analyzer analyzer) throws Exception{  
        Directory dire=FSDirectory.open(new File(Constants.INDEX_STORE_PATH));  
        IndexWriterConfig iwc=new IndexWriterConfig(Version.LUCENE_46, analyzer);  
        IndexWriter iw=new IndexWriter(dire, iwc);  
        LuceneIndex.addDoc(iw);  
        iw.close();  
    }  
      
    /**  
     * ��̬���Document  
     * @param iw  
     * @throws Exception  
     */  
    public static void addDoc(IndexWriter iw)  throws Exception{  
    	Connection conn=DbUtil.getConnection();//��ȡ���ݿ�����  
    	String sql = "select title,content from news";
    	//sqlִ��������  
    	 Statement stmt = conn.createStatement();
        //���������  
        ResultSet rs= stmt.executeQuery(sql);
        while(rs.next()){
        	Document doc=new Document();  
        	String title = rs.getString("title");
        	String content = rs.getString("content");
        	doc.add(new TextField("title", StringUtils.isEmpty(title)?"":title, Store.YES));  
            doc.add(new TextField("content", StringUtils.isEmpty(content)?"":content, Store.YES));  
            iw.addDocument(doc);  
            iw.commit();  
        }
        DbUtil.close(rs);
        DbUtil.close(conn);
    	/* File[] files=new File(Constants.INDEX_FILE_PATH).listFiles();  
        for (File file : files) {  
            Document doc=new Document();  
            String content=LuceneIndex.getContent(file);  
            String name=file.getName();  
            String path=file.getAbsolutePath();  
            doc.add(new TextField("title", content, Store.YES));  
            doc.add(new TextField("content", name, Store.YES));  
            System.out.println(name+"==="+content+"==="+path);  
            iw.addDocument(doc);  
            iw.commit();  
        }  */
    }  
      
    /**  
     * ��ȡ�ı�����  
     * @param file  
     * @return  
     * @throws Exception  
     */  
    @SuppressWarnings("resource")  
    public static String getContent(File file) throws Exception{  
        FileInputStream fis=new FileInputStream(file);  
        InputStreamReader isr=new InputStreamReader(fis);  
        BufferedReader br=new BufferedReader(isr);  
        StringBuffer sb=new StringBuffer();  
        String line=br.readLine();  
        while(line!=null){  
            sb.append(line+"\n");  
            line=null;  
        }  
        return sb.toString();  
    }  
      
    /**  
     * ����  
     * @param query  
     * @throws Exception  
     */  
    private static void search(Query query,Analyzer analyzer) throws Exception {  
        Directory dire=FSDirectory.open(new File(Constants.INDEX_STORE_PATH));  
        IndexReader ir=DirectoryReader.open(dire);  
        IndexSearcher is=new IndexSearcher(ir);  
        TopDocs td=is.search(query, 1000);  
        System.out.println("��Ϊ�����ҵ�"+td.totalHits+"�����");  
        ScoreDoc[] sds =td.scoreDocs;
        //�˴����������������ĸ�������
        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color=red>","</font></b>"); //�����ָ�������Ļ���Ĭ���ǼӴ֣���<b><b/>
        QueryScorer scorer = new QueryScorer(query);//����÷֣����ʼ��һ����ѯ�����ߵĵ÷�
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer); //��������÷ּ����һ��Ƭ��
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
        highlighter.setTextFragmenter(fragmenter); //����һ��Ҫ��ʾ��Ƭ��
        for (ScoreDoc sd : sds) {
            Document d = is.doc(sd.doc);   
//            System.out.println(d.get("path") + ":["+d.get("path")+"]");
            String content = d.get("content");
            TokenStream tokenStream = analyzer.tokenStream("desc", new StringReader(content));
            String summary = highlighter.getBestFragment(tokenStream, content);
            System.out.println(summary);
        }  
    }  
      
      
   public static void main(String[] args) throws Exception, Exception {  
	   Long beginTime = System.currentTimeMillis();
        Analyzer analyzer=new StandardAnalyzer(Version.LUCENE_46);  
//        LuceneIndex.createIndex(analyzer);  
        QueryParser parser = new QueryParser(Version.LUCENE_46, "content", analyzer);   
        Query query = parser.parse("����");  
        LuceneIndex.search(query,analyzer);  
        Long endTime = System.currentTimeMillis();
        System.out.println("�����ʱ:"+(endTime-beginTime));
//	   Long beginTime = System.currentTimeMillis();
//        Connection conn=DbUtil.getConnection();//��ȡ���ݿ�����  
//    	String sql = "select title,content from news where content like '%����%'";
//    	//sqlִ��������  
//    	 Statement stmt = conn.createStatement();
//        //���������  
//        ResultSet rs= stmt.executeQuery(sql);
//        Long endTime = System.currentTimeMillis();
//        System.out.println("�����ʱ:"+(endTime-beginTime));
//        while(rs.next()){
//        	String title = rs.getString("title");
//        	String content = rs.getString("content");
//        	System.out.println(title);
//        }
//        DbUtil.close(rs);
//        DbUtil.close(conn);
    }  
}
