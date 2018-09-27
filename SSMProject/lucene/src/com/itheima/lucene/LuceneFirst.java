package com.itheima.lucene;


import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;

public class LuceneFirst {

    private  Directory directory;
    private  IndexWriter indexWriter;
    private  IndexSearcher indexSearcher;
    private  IKAnalyzer ikAnalyzer;
    private  IndexReader indexReader;
    @Before
    public  void init() throws IOException {
       directory = FSDirectory.open(new File("E:\\writer"));
        ikAnalyzer = new IKAnalyzer();
        indexWriter = new IndexWriter(directory, new IndexWriterConfig(Version.LATEST, ikAnalyzer));
       indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
        indexReader = DirectoryReader.open(directory);
    }

    public void  test(){
        System.out.println("2222222222222");
    }

    @Test
    public void testCreateIndex() throws IOException {
        //1）创建目录，在代码中创建一个Directory对象，指定索引库保存的目录。
        Directory directory = FSDirectory.open(new File("E:\\writer"));
        //2）创建一个IndexWriter对象，两个参数一个directory对象，IndexWriterConfig对象
        Analyzer analyzer = new IKAnalyzer();
        //IndexWriterConfig对象直接创建，两个参数，Lucene对应的 版本号，分析器对象StandardAnalyzer。
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        //3）读取磁盘上的文件，取文件的属性
        File file = new File("E:\\IndexReository");
        File[] files = file.listFiles();
        for (File file1 : files) {
            //文件名
            String file1Name = file1.getName();
            //文件路径
            String path = file1.getPath();
            //文件内容
            String content = FileUtils.readFileToString(file1);
            //文件大小
            long size = FileUtils.sizeOf(file1);
            //4）把属性封装到Field中
            //参数1：域的名称
            //参数2：域的值
            //参数3：是否存储。如果存储就可以取出域的内容，如果不存储就不能取出域的内容。
            Field fieldName = new TextField("name", file1Name, Field.Store.YES);
            Field fieldpath = new TextField("path", path, Field.Store.YES);
            Field fieldcontent = new TextField("content", content, Field.Store.YES);
            Field fieldsize = new TextField("size", size+"", Field.Store.YES);
            //5）创建一个Document对象
            Document document = new Document();
            //6）把field添加到文档对象中
            document.add(fieldName);
            document.add(fieldpath);
            document.add(fieldcontent);
            document.add(fieldsize);
            //7）把Document对象写入索引库
            indexWriter.addDocument(document);
        }
        //8）关闭IndexWriter
        indexWriter.close();
    }

    @Test
    public void  testQueryIndex() throws IOException {
        // 1）创建一个Directory对象，指定索引库的位置
        Directory directory = FSDirectory.open(new File("E:\\writer"));
        // 2）创建一个IndexReader对象
        IndexReader indexReader = DirectoryReader.open(directory);
        // 3）创建一个IndexSearcher对象，需要基于IndexReader创建
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        // 4）创建一个Query对象，TermQuery，根据关键词查询，需要指定要查询的域及关键词。
        //参数1：要查询的域
        //参数2：要查询的关键词
        Query query = new TermQuery(new Term("content", "is"));
        // 5）执行查询，使用IndexSearcher对象执行查询。
        //参数1：查询对象 参数2：返回结果的最大记录数
        TopDocs topDocs = indexSearcher.search(query, 10);
        // 6）查询结果中可以取查询结果的总记录数。
        int totalHits = topDocs.totalHits;
        System.out.println("最大记录数:"+totalHits);
        // 7）遍历查询结果，取结果列表
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            //取文档的id
            int docId = scoreDoc.doc;
            //根据文档的id取文档对象
            Document doc = indexSearcher.doc(docId);
            //取域的内容
            System.out.println(doc.get("name"));
//            System.out.println(doc.get("content"));
            System.out.println(doc.get("size"));
            System.out.println(doc.get("path"));
        }

        // 8）关闭IndexReader对象
        indexReader.close();
    }

    @Test
    public void testcondition() throws IOException {
        // 1、创建一个分析器对象。
        //Analyzer analyzer = new StandardAnalyzer();
        //Analyzer analyzer = new CJKAnalyzer();
        //Analyzer analyzer = new SmartChineseAnalyzer();
        Analyzer analyzer = new IKAnalyzer();
        // 2、调用分析器对象的TokenStream方法返回一个TokenStream对象。
        //参数1:域的名称。可以是null
        //参数2：要分析的文本内容。
        TokenStream tokenStream = analyzer.tokenStream(null, "博主波波什么是CJKCJK是由WernerLemberg传智播客开发的支持中、日、韩、英文字的宏包。CJK的取名就是这三国英文名字的首字母。法轮功可以从http://cjk.ffii.org/网站免费下载。");
        // 3、向TokenSTream对象中设置引用，相当于设置一个指针。
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        // 4、调用TokenStream对象的reset方法。
        tokenStream.reset();
        // 5、使用while遍历TokenStream中的内容
        while(tokenStream.incrementToken()) {
            //取关键词
            System.out.println(charTermAttribute.toString());
        }
        // 6、关闭TokenStream
        tokenStream.close();
    }


    //域的三个属性  :1.是否分析2.是否索引3.是否存储
    //StringField:      N         Y       Y/N
    //TextField:        Y         Y       Y/N
    //LongField:        Y         Y       Y/N
    //StoredField:      N         N        Y
    //新增文档
    @Test
    public void testFild() throws IOException {
        Document document = new Document();

        TextField textField = new TextField("content", "你说啥?我听不见! I can not see you!", Field.Store.NO);
        StoredField storedField = new StoredField("path", "C:1.txt");
        TextField name = new TextField("name", "aaa.txt", Field.Store.YES);
        document.add(textField);
        document.add(storedField);
        document.add(name);
        indexWriter.addDocument(document);
        indexWriter.close();
    }
    //删除文档
    @Test
    public void testDelet() throws IOException {
            //使用indexWriter 根据查询结果删除 提供term对象
        indexWriter.deleteDocuments(new Term("name","aaa"));
        indexWriter.close();
    }
    //修改文档
    @Test
    public void testUpdate() throws IOException {
        //创建一个新的文档
        //使用indexWriter 的update方法 两个参数 一个是新的文档 一个是查询条件,原理是 先查询再删除再添加
        Document document = new Document();
        TextField textField = new TextField("content", "更新的内容 !你说啥?我听不见! I can not see you!", Field.Store.YES);
        StoredField storedField = new StoredField("path", "C:1.txt");
        TextField name = new TextField("name", "bbb.txt", Field.Store.YES);
        document.add(textField);
        document.add(storedField);
        document.add(name);
        indexWriter.updateDocument(new Term("name","aaa"),document);
        indexWriter.close();
    }
    //索引库的查询
    //1、使用Query的子类进行查询

    private void searchResult(Query query) throws Exception {
        //执行查询
        TopDocs topDocs = indexSearcher.search(query, 10);
        System.out.println("总记录数：" + topDocs.totalHits);
        //取文档列表
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get("name"));
            //System.out.println(document.get("content"));
            System.out.println(document.get("size"));
            System.out.println(document.get("path"));
        }
        indexReader.close();
    }
    //1）TermQuery:根据关键词查询,需要指定被查询的域和要查询的关键字词
    //2) MatchAllDocsQuery:查询所有文档

    @Test
    public void  MatchAllDocsQuery() throws Exception {
        //1、创建一个Directory对象，指定索引库的位置

        //2、创建一个IndexReader对象。

        //3、创建一个IndexSearcher，基于IndexReader创建

        //4、创建一个Query对象
        Query query = new MatchAllDocsQuery();
        //5、执行查询
        System.out.println(query);
        //6、取查询结果
        searchResult(query);
        //7、关闭IndexReader
        indexReader.close();
    }
    //NumericRangeQuery 数值范围查询
    @Test
    public void  NumericRangeQuery() throws Exception {
        //创建一个查询对象
        //参数1：要查询的域，参数2：最小值，参数3：最大值，参数4：是否包含最小值，参数5：是否包含最大值
        Query query = NumericRangeQuery.newLongRange("size", 1L, 10000L, true, true);
        System.out.println(query);
        searchResult(query);
    }


    //BooleanQuery:多条件组合查询
    //Occur.SHOULD :相当于or
    //Occur.Must    :相当于and
    //Occur.Must_Not:相当于not
    @Test
    public void BooleanQuery() throws Exception{
        BooleanQuery query = new BooleanQuery();
        //添加查询条件
        TermQuery query1 = new TermQuery(new Term("name","test"));
        TermQuery query2 = new TermQuery(new Term("name", "bbb"));
        query.add(query1, BooleanClause.Occur.SHOULD);
        query.add(query2, BooleanClause.Occur.SHOULD);
        System.out.println(query);
        searchResult(query);
    }


    //2、使用QueryParser
    //待分析的查询,可以先对要查询的内容进行分词,然后基于分词的结果进行查询
    //）创建一个QueryParser对象，构造方法中两个参数
    //			参数1：默认搜索域，不指定在哪个域上查询时使用默认搜索域。
    //			参数2：分析器对象
    //		2）使用QueryParser对象的parse方法基于要查询的内容创建一个Query对象。
    //		3）执行查询
    @Test
    public void QueryParser() throws Exception {
        QueryParser queryParser = new QueryParser("name", new IKAnalyzer());
        Query query = queryParser.parse("name:bbb content:t");
        System.out.println(query);
        searchResult(query);

    }
}
