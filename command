java -Xmx8G -jar tagsuggest.jar evaluation.CrossValidator --dataset=/data/disk1/private/ydm/data/post.dat --trainer_class=TrainWAMsample --suggester_class=SMTTagSuggest --num_folds=5 --config="dataType=Post;para=1;" --working_dir=/data/disk1/private/ydm/working_dir2/ --at_n=10 --report=/data/disk1/private/ydm/reportkeyword/evaluation_WAMsample.txt


java -Xmx8G -jar tagsuggest.jar evaluation.CrossValidator --dataset=/data/disk1/private/ydm/data/keypost.dat --trainer_class=TrainWAM --suggester_class=SMTTagSuggest --num_folds=5 --config="dataType=Post;para=1;scoreLimit=0.1" --working_dir=/data/disk1/private/ydm/working_dir/ --at_n=10 --report=/data/disk1/private/ydm/reportkeyword/evaluation_WAM.txt


java -Xmx8G -jar tagsuggest.jar evaluation.CrossValidator --dataset=/data/disk1/private/ydm/data/keypost.dat --trainer_class=TrainTFIDF --suggester_class=TFIDFTagSuggest --num_folds=5 --config="dataType=Post;" --working_dir=/data/disk1/private/ydm/working_dir2/ --at_n=10 --report=/data/disk1/private/ydm/reportkeyword/evaluation_TFIDF.txt

java -Xmx8G -jar tagsuggest.jar evaluation.CrossValidator --dataset=/data/disk1/private/ydm/data/keypost.dat --trainer_class=TrainKnn --suggester_class=KnnTagSuggest --num_folds=5 --config="dataType=Post;k=5;" --working_dir=/data/disk1/private/ydm/working_dir/ --at_n=10 --report=/data/disk1/private/ydm/reportkeyword/evaluation_Knn.txt


java -Xmx8G -jar tagsuggest.jar evaluation.CrossValidator --dataset=/data/disk1/private/ydm/data/keypost.dat --trainer_class=TrainTagLdaModel --suggester_class=TagLdaTagSuggest --num_folds=5 --config="dataType=Post;numtopics=32;niter=40" --working_dir=/data/disk1/private/ydm/working_dir/ --at_n=10 --report=/data/disk1/private/ydm/reportkeyword/evaluation_TagLdaModel.txt

java -Xmx8G -jar tagsuggest.jar evaluation.CrossValidator --dataset=/data/disk1/private/ydm/data/post.dat --trainer_class=TrainTAM --suggester_class=TAMTagSuggest --num_folds=5 --config="dataType=Post;niter=40;numBurnIn=30" --working_dir=/data/disk1/private/ydm/working_dir/ --at_n=10 --report=/data/disk1/private/ydm/reportkeyword/evaluation_TAM.txt

java -Xmx8G -jar tagsuggest.jar evaluation.CrossValidator --dataset=/data/disk1/private/ydm/data/keypost.dat --trainer_class=TrainNoiseTagLdaModel --suggester_class=NoiseTagLdaTagSuggest --num_folds=5 --config="dataType=Post;numtopics=64;niter=40;" --working_dir=/data/disk1/private/ydm/working_dir3/ --at_n=10 --report=/data/disk1/private/ydm/reportkeyword/evaluation_NoiseTagLdaModel.txt

java -Xmx8G -jar tagsuggest.jar evaluation.CrossValidator --dataset=/data/disk1/private/ydm/data/keypost.dat --trainer_class=TrainExpandRank --suggester_class=ExpandRankKE --num_folds=5 --config="dataType=Post;" --working_dir=/data/disk1/private/ydm/working_dir3/ --at_n=10 --report=/data/disk1/private/ydm/reportkeyword/evaluation_ExpandRankKE.txt

java -Xmx8G -jar tagsuggest.jar evaluation.CrossValidator --dataset=/data/disk1/private/ydm/data/keypost.dat --trainer_class=TrainWAMWithtitleInstead --suggester_class=SMTTagSuggest --num_folds=5 --config="dataType=Post;para=1;" --working_dir=/data/disk1/private/ydm/working_dir/ --at_n=10 --report=/data/disk1/private/ydm/reportkeyword/evaluation_TrainWAMWithtitleInstead.txt

java -Xmx8G -jar tagsuggest.jar evaluation.CrossValidator --dataset=/home/meepo/test/sample/bookPost70000.dat --trainer_class=TrainWTM --suggester_class=SMTTagSuggest --num_folds=5 --config="dataType=DoubanPost;para=0.5;" --working_dir=/home/meepo/test/sample --at_n=10 --report=/home/meppo/evaluation_WTM.txt

java -Xmx8G -jar tagsuggest.jar evaluation.CrossValidator --dataset=/data/disk1/private/ydm/data/bookPost70000.dat --trainer_class=TrainPMI --suggester_class=PMITagSuggest --num_folds=5 --config="dataType=DoubanPost;commonLimit=5;selfTrans=0;" --working_dir=/data/disk1/private/ydm/working_dir/ --at_n=10 --report=/data/disk1/private/ydm/report/evaluation_PMI.txt

