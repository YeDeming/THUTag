# THUTag: A Package of Kephrase Extraction and Social Tag Suggetion 
==============

Table of Content
==============

Part I   : THUTag Contents

Part II  : How To Compile THUTag

Part III : How To Run Cross-validation of THUTag

Part IV  : Input File Formats of Cross-validation

Part V   : Output File Formats of Cross-validation

Part VI  : How To Run UI && Testing a single passage of THUTag

Part VII : Input File Formats of UI && Testing a single passage

Part VIII: Output File Formats of UI && Testing a single passage

Part IX  : Literature

Part X   : License

Part XI  : Authors

Part XII : Appendix


Part I: THUTag Contents
==============

The package contains mutiple algorithms for Keyphrase Extraction and Social Tag Suggestion including a Cross-Validation Evaluator.

build :　Working directory

GIZA++  mkcls  plain2snt.out : Essential to running WTM/WAM/WAM*

Part II: How To Compile THUTag
==============

Environment : java (support java 1.8.0)

ant : Start a terminal in the directory "THUTag/", input command "ant" and then THUTag will be compiled; or you can choose the build.xml for "ant build" in eclipse.


Part III: How To Run Cross-validation of THUTag
==============

There are a lot of examples in file "Command_Example".And there is a demo in "/demo".You'd use commandline to run the algorithm beacuse it need to set the working RAM.

Test a single algorithm using Cross Evaluation : Specific commands can be found in file "command", a Training Class is corresponding to exactly a Suggesting Class. (Part VII Appendix show the correspondence between Training Class and Suggesting Class)

Parameters : --dataset="Input file path"
	      --trainer_class="Name of Training Class"
	      --suggester_class="Name of Suggesting Class"
	      --num_folds="Num the file divided into"
	      --config="Parameter1=Value1;Parameter2=Value2;..."(The parameters can be dataType, k, numtopics, niter, etc.. Parameters vary with models and no parameter means using default values)
	      --working_dir="Working directory,a Working directory is used for storing a model for one algorithm"
	      --report="Path of report"

e.g.
java -Xmx5G -jar tagsuggest.jar evaluation.CrossValidator --dataset=/data/disk1/private/ydm/data/bookPost70000.dat --trainer_class=TrainWTM --suggester_class=SMTTagSuggest --num_folds=5 --config="dataType=DoubanPost;para=0.5;minwordfreq=10;mintagfreq=10;selfTrans=0.2;commonLimit=2" --working_dir=/data/disk1/private/ydm/tryWTM/ --at_n=10 --report=/data/disk1/private/ydm/report/evaluation_WTMreal.txt

-Xmx5G is set the working RAM.

The default path of book.model and chinese_stop_word.txt is the same with the path of tagsuggest.jar. If you need change the path of them, you should add model="Path of both of them" to --config. "book.model" is used for maximum forward word-segmentation for data that focus on books and "chinese_stop_word" records the stop words in chinese. e.g. --config="model=/home/meepo/TagSuggestion;dataType=DoubanPost;"

If you want to run SMT, you need another three files GIZA++, mkcls, and plain2snt.out. Their default path is the same with the path of tagsuggest.jar.And if you want to work in Windows,you must compile the GIZA++, mkcls, and plain2snt.out again.They are in /giza-pp and compile by make.Then you should fix some code in TrainWTM/TrainWAM*,replacing "GIZA++" with "GIZA++.exe","mkcls" with "mkcls.exe","plain2snt.out" with "plain2snt.out.exe".

If you need change the path of them, you should add --giza_path="Path of them" as a parameter to the command.

The evaluation results on Douban Post Dataset (M_d=3,select the three tags with the highest value)

| Algorithm | Precision | Recall | F1 |
|---|:---|:---|:---|
| PMI | 0.38962	| 0.45730 | 0.36692 |
| WTM | 0.36828 | 0.45131 | 0.35410 |
| KNN |	0.33910 | 0.37885 | 0.31103 |
| TAM | 0.30758 | 0.34045 | 0.28093 |
| NaiveBayes |	0.27064 | 0.30223 | 0.24671 |
| NoiseTagLdaModel | 0.20956 | 0.20757 | 0.18054 |
| TagLdaModel | 0.15756 | 0.16646 | 0.14054 |

The evaluation results on Keyword Post Dataset (M_d=2,select the two keywords with the highest value)

| Algorithm | Precision | Recall | F1 |
|---|:---|:---|:---|
| WAM | 0.30735 | 0.43726 | 0.34747 |
| WAMsample | 0.29424 | 0.41814 | 0.33254 |
| WAMwithtitleInstead | 0.26571 | 0.37286 | 0.29849 |
| ExpandRankKE | 0.22818 | 0.31578 | 0.25461 |
| TPR | 0.21913 | 0.3060 | 0.24551 |
| TFIDF | 0.25459 | 0.20083 | 0.21876 |
| Textpagerank | 0.19833 | 0.22971 | 0.20837 |


Part IV: Datasets and Formats for Evaluation
==============

We share two datasets in "traindata/" for evaluation. The both datasets are in Chinese. The KeywordPost.dot is from NetEase News, which can be used for keyphrase extraction algorithm. The bookPost70000.dat and post.dat are from Douban Book, which can be used for social tag suggestion algorithm. The formats of these datasets are as follows:

dataType=Post : {"id":"document id","content":"document content","extras":"","resourceKey":"","timestamp":0,"title":"document title","userId":"","tags":["tag1","tag2","tag3",...]}   (Focus on books)

Example : {"id":"1004838","content":"格非――1964年生于江苏省丹德县，毕业于华东师范大学中文系，获文学博士学位。1985年至2000年任教于华东师范大学，现为清华大学中文系教授。主要作品有《格非文集》、《欲望的旗帜》等。","extras":"","resourceKey":"","timestamp":0,"title":"敌人","userId":"","tags":["小说","荒诞","敌人","中国文学","格非"]}   (Demo file is post.dat)

dataType=DoubanPost : {"doubanTags":{"tag1":weight,"tag2":weight,"tag3":weight,...},"id":"document id","content":"document content","tags":[empty],"timestamp":0,"resourceKey":"","title":"document title","userId":"","extras":""}   (Focus on books)

Example : {"doubanTags":{"文化":5,"献给非哲学家的小哲学":6,"哲学":29,"法国":17},"id":"1000047","content":"全球化是必然趋势？仁者见仁，智者见智。有人惊呼：“狼来了！”有人担忧：“怎么办？”还有人在思考：“对世界来说，经济可以全球化，甚至货币也可以一体化，但文化则要鼓励多元化。”是的，只有本着文化多元化的精神，在尊重其他民族文化的同时，自身才能获得不断的发展与丰富。法国人做出了自己的探索与努力。今天，您面前的这一套“法兰西书库·睿哲系列”为您打开了一扇沟通的窗口。他山之石，可以攻玉。我们希望这样的对话可以走得越来越远。","tags":[],"timestamp":0,"resourceKey":"","title":"献给非哲学家的小哲学  睿哲系列","userId":"","extras":""}   (Demo file is bookPost70000.dat)

The difference of the above two is that bookPost70000.dat is used for WTM/PMI because they need to know the weight of tags.And the other social tag suggestion algorithm uses post.dat.(see file "Command_Example")

dataType=KeywordPost : {"date": "news date","summary":"news summary"，"source":"news source","id":"document id","content":"document content","title":"news title","resourceKey":"","extras":"","userId":"","tags":["tag1","tag2","tag3",...]}  (Focus on news)

Example : {"date":"2010-6-12 3:39:39","summary":"核心提示：重庆市政府公众信息网发布消息称，经2010年5月13日市政府第70次常务会议通过，给予文强、陈洪刚二人行政开除处分。","source":"http://news.163.com/10/0612/03/68USU60D000146BD.html","id":"0","content":"重庆晚报6月11日报道  昨日，市政府公众信息网发布消息称，经2010年5月13日市政府第70次常务会议通过，给予文强、陈洪刚二人行政开除处分。\n今年4月14日，市第五中级人民法院以受贿罪，包庇、纵容黑社会性质组织罪，巨额财产来源不明罪，强奸罪数罪并罚判处文强死刑，剥夺政治权利终身，并处没收个人全部财产。5月21日，市高级人民法院对文强案二审宣判，依法驳回文强上诉，维持一审的死刑判决。\n2月25日，市公安局交警总队原总队长陈洪刚受贿案在市第五中级人民法院一审宣判。陈洪刚因犯受贿，包庇、纵容黑社会性质组织，巨额财产来源不明，伪造居民身份证罪，数罪并罚，被判处有期徒刑20年，没收个人财产40万元人民币，追缴赃款326万余元及不明来源财产584万余元。记者 李伟\n","title":"重庆市政府给予文强行政开除处分","timestamp":0,"resourceKey":"","userId":"","tags":["文强","重庆"],"extras":""} (Demo file is KeywordPost.dat)

The KeywordPost.dat is used for keyphrase extraction algorithm.

Part V: Output File Formats of Cross-validation
==============

The output is a text file,whose first seven columns are the major data.From the first column to the seventh column are these in order: the number of keywords that we ask the algorithm to output | precision rate(Pre.) | the variance of precision rate | recall rate(Rec.) | the variance of recall rate | Fmeans | the variance of Fmeans

we have that 2 / Fmeans = 1 / Pre. +1 / Rec.

Part VI: How To Run UI & Test a Single Passage with THUTag
==============

Command for training model : java -Xmx8G -jar tagsuggest.jar train.TrainWTM --input=/home/meepo/test/sampleui/bookPost70000.dat --output=/home/meepo/test/sample --config="dataType=DoubanPost;para=0.5;minwordfreq=10;mintagfreq=10;selfTrans=0.2;commonLimit=2" 

"input" is the train data's address.
"output" is where the model will be set.
"config"  is the config of the model.

 
Command for running UI : java -Xmx8G -jar tagsuggest.jar evaluation.GuiFrontEnd --model_path=/home/meepo/test/sampleui/  --algorithm=SMTTagSuggest --config="" --realtime=true 

"model_path" is the model's address.
"algorith" is the train class that we choose.
"config" is the config of thetrain class.

 
Test a single passage : java -Xmx8G -jar tagsuggest.jar evaluation.TestDemo --model_path=/home/meepo/test/sampleui/ --algorithm=SMTTagSuggest --config="" --article_path=/home/meepo/text --output_path=/home/meepo/tag 
  
"model_path" is the model's address.
"algorith" is the train class that we choose.
"config" is the config of thetraiclass.
 
 
The default path of book.model and chinese_stop_word.txt is the same with the path of tagsuggest.jar. If you need change the path of them, you should add model="Path of both of them" to --config. "book.model" is used for maximum forward word-segmentation for data that focus on books and "chinese_stop_word" records the stop words in chinese. e.g. --config="model=/home/meepo/TagSuggestion;dataType=DoubanPost;"
 
If you want to run WTM/WAM/WAM*, you need another three files GIZA++, mkcls, and plain2snt.out. Their default path is the same with the path of tagsuggest.jar.If you need change the path of them, you should add --giza_path="Path of them" as a parameter to the command. 


Part VII: Input File Formats of UI & Testing a Single Passage
==============

In the UI interface,you can input text directly.
And when test a individual text file,the text file must contains two lines:the first line is the title of the article and the second line is the content of the article.

Part VIII: Output File Formats of UI & Testing a Single Passage
==============

In the UI interface,our program will show the keywords to the screen directly.
And when test a individual text file,the program will give back a text file with ten keywords that the algorithm forecast and their corresponding weights.

Part IX: Literature
==============
If you are using the package, please acknowledge the package by citing the paper:
	
	Xinxiong Chen, Deming Ye, Xiance Si, Zhiyuan Liu and Maosong Sun. THUTag: A Package for Keyphrase Extraction and Social Tag Suggestion. 2016.

If you’re dealing in depth with particular algorithms, you are also encouraged to cite the papers that cover individual algorithms as follows:

| Keyphrase Extraction | Papers |
|---|:---|
| WAM | Zhiyuan Liu, Xinxiong Chen, Yabin Zheng, Maosong Sun. Automatic Keyphrase Extraction by Bridging Vocabulary Gap. The 15th Conference on Computational Natural Language Learning (CoNLL 2011).  |
| TPR | Zhiyuan Liu, Wenyi Huang, Yabin Zheng, Maosong Sun. Automatic Keyphrase Extraction via Topic Decomposition. The Conference on Empirical Methods in Natural Language Processing (EMNLP 2010), 2010. |
| ExpandRank | Xiaojun Wan, Jianguo Xiao. Single Document Keyphrase Extraction Using Neighborhood Knowledge. The 23rd AAAI Conference on Artificial Intelligence (AAAI 2008). |
| TextRank | Mihalcea, R. and Tarau, P. TextRank: Bringing order into texts. The Conference on Empirical Methods in Natural Language Processing (EMNLP 2004). |


| Social Tagging | Papers |
|---|:---|
| PMI | Xinxiong Chen, Zhiyuan Liu, Maosong Sun. Estimating Translation Probabilities for Social Tag Suggestion. Expert Systems With Applications. |
| TagLda | Xiance Si, Maosong Sun. Tag-LDA for scalable real-time tag recommendation. Journal of Computational Information Systems 6 (1), 23-31. |
| TAM | Xiance Si, Zhiyuan Liu, Maosong Sun. Modeling Social Annotations via Latent Reason Identification. IEEE Intelligent Systems, 2010. |
| WTM | Zhiyuan Liu, Xinxiong Chen, Maosong Sun. A Simple Word Trigger Method for Social Tag Suggestion. The Conference on Empirical Methods in Natural Language Processing (EMNLP 2011). |

Part X: License
==============

THUTag is licensed under the GNU General Public License (v3 or later). Note that the license is the full GPL, which allows many free uses, but not its use in proprietary software which is distributed to others. For distributors of proprietary software, commercial licensing is available from Tsinghua University. You can contact us at thunlp@gmail.com .

Part XI: Authors
==============

Contributors: Xinxiong Chen, Deming Ye, Xiance Si, Zhiyuan Liu.

Supervisor: Prof. Maosong Sun. 

For any questions about this package, you can contact us at thunlp@gmail.com .

Part XII: Appendix
==============
The correspondence between Training Class and Suggesting Class
There are a lot of examples in file "Command_Example".

| Training Class | Suggesting Class |
|---|:---|
| TrainExpandRank | ExpandRankKE |
| TrainKnn | KnnTagSuggest |
| TrainNaiveBayes | NaiveBayesTagSuggest |
| TrainNoiseTagLdaModel | NoiseTagLadaTagSuggest |
| TrainPMI | PMITagsuggest |
| TrainTagLdaModel | TagLdaTagSuggest |
| TrainTAM | TAMTagSuggest |
| TrainTFIDF | TFIDFTagSuggest |
| TrainTopicPageRank | TopicPageRankTagSuggest |
| TrainWAM | SMTKeywordTagSuggest/SMTTagSuggest |
| TrainWAMsample | SMTKeywordTagSuggest/SMTTagSuggest |
| TrainWAMWithtitleInstead | SMTTagSuggest | 
| TrainWTM | SMTTagSuggest |
| TrainTextpagerank | TextpagerankTagSuggest |
