package org.grouplens.lenskit.paperAdvisor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.config.ConfigHelpers;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import java.io.FileWriter;
import java.sql.Timestamp;

public class PaperAdvisor implements Runnable {

    //main
    public static void main(String[] args) throws IOException {
       /* PaperAdvisor paperadvisor = new PaperAdvisor(args);
      
        paperadvisor.execRec(10L, 10L);
        //paperadvisor.run();
        */
       Frame frame = new Frame();
       frame.setVisible(true);
    }

    //private variables
    //private List<Long> userIds = new ArrayList<>();
    //private List<Long> userNRec = new ArrayList<>();

    private Long userId;
    private Long nRec;

    private static final Logger printLogs = LoggerFactory.getLogger(PaperAdvisor.class);

    private Path path = Paths.get("data/movielens.yml");

    private File groovy = null;
    

    static List<Long> popIds = new ArrayList<>();
    static List<String> popTitle = new ArrayList();

    static List<Long> itemIds = new ArrayList<>();
    static List<String> itemTitle = new ArrayList();

    static List<Long> userIds = new ArrayList<>();
    static List<String> userTitle = new ArrayList();

    //recMethod:
    //1- item-item
    //2- user-user
    //3- Popularity rank
    private int recMethod;
    
    //public methods

    public PaperAdvisor() {
       /* Boolean flag=true;
        int i = 0;
        for (String id: args) {
            if(i==0){
                recMethod = Integer.parseInt(id);
                i++;
            }
            else if(i == 1) {
                userId = (Long.parseLong(id));
                i++;
            }
            else if(i==2){
                nRec = (Long.parseLong(id));
                i--;
            }
            else {
                System.out.println("INVALID RECOMMENDATION METHOD!");
            }


        }   */
        List<String> coldstart = new ArrayList();

        for(Long j = 1L; j <11L; j++)
        {
            popIds.add(j);
            itemIds.add(j);
            userIds.add(j);
        }

        coldstart.add("Desenvolvendo um Sistema de Recomendacao de Objetos de Aprendizagem baseado em Competencias para a Educacao: relato de experiencias");
        coldstart.add("Recommender systems in technology enhanced learning");
        coldstart.add("A K-Means Clustering Algorithm");
        coldstart.add("What is an Ontology?");
        coldstart.add("The Role of Trust and Deception in Virtual Societies");
        coldstart.add("Sharing a Secret: How Kerberos Works");
        coldstart.add("Using trust in recommender systems: an experimental analysis");
        coldstart.add("What drives a successful e-Learning? An empirical investigation of the critical factors influencing learner satisfaction");
        coldstart.add("Constructionist Design Methodology for Interactive Intelligences");
        coldstart.add("A knowledge and collaboration-based CBR process to improve network performance-related support activities");

        popTitle = coldstart;
        itemTitle = coldstart;
        userTitle = coldstart;
                 
    }

    public void execRec(Long user, Long recNumber)
    {

        //recMethod:
        //1- item-item
        //2- user-user
        //3- Popularity rank
        userId = user;
        nRec = recNumber;
        recMethod = 1;

        run();

        userId = user;
        nRec = recNumber;
        recMethod = 2;

        run();

        userId = user;
        nRec = recNumber;
        recMethod = 3;

        run();
    }

    public void run() {

        //lenskit config
        LenskitConfiguration configLenskit = null;

        //recMethod:
        //1- item-item
        //2- user-user
        //3- Popularity rank
        File groovy = new File("etc/popularityRank.groovy");
        if(recMethod == 1){

            groovy = new File("etc/item-item.groovy");
        }
        else if(recMethod == 2){
            groovy = new File("etc/user-user.groovy");
        }
        else if(recMethod == 3){
            groovy = new File("etc/popularityRank.groovy");
        }
        else {
            System.out.println("INVALID RECOMMENDATION METHOD \n DEFAULT POPULARITY RANK WILL BE USED");
        }

        try {
            configLenskit = ConfigHelpers.load(groovy);
        } catch (IOException e) {
            throw new RuntimeException("LenskitConfiguration", e);
        }

        //get data from files
        DataAccessObject dao;

        try {
            StaticDataSource data = StaticDataSource.load(path);
            
            dao = data.get();
        } catch (IOException e) {
            printLogs.error("data", e);
            throw Throwables.propagate(e);
        }


        LenskitRecommenderEngine engineLenskit = LenskitRecommenderEngine.build(configLenskit, dao);
        LenskitRecommender recommender = engineLenskit.createRecommender(dao);
        printLogs.info("recommender created");
            
        ItemRecommender itemRec = recommender.getItemRecommender();
        assert itemRec != null; //recommender configured -> !=null
       
        //print recommendations

        int idUser = userId.intValue();
        int numberRec = nRec.intValue();

        List<Long> indices = new ArrayList<>();

        List<String> titles = new ArrayList<>();
        
        ResultList recommendations = itemRec.recommendWithDetails(idUser, numberRec, null, null);
        //System.out.println("\nPapers recommended to user  " +idUser+ " :\n");
        try{
            for (int i = 0 ; i < numberRec ; i++) { //recommend papers

                Result paper = recommendations.get(i);

                Entity entityPaper = dao.lookupEntity(CommonTypes.ITEM, paper.getId());
                String namePaper = "";

                if (entityPaper != null) {
                    namePaper = entityPaper.maybeGet(CommonAttributes.NAME);
                }

                //System.out.print("\t" + i + "- Title = " + namePaper + " | id = " + paper.getId() + " | score = " +paper.getScore());
                //System.out.printf("%.2f\n", paper.getScore());
                indices.add(paper.getId());
                titles.add(namePaper);


                //System.out.format("\t %d - Title = %s | id = %d | score = %.2f\n", (i+1), namePaper, paper.getId(), paper.getScore());

            }

            if(recMethod == 1){
                itemIds = indices;
                itemTitle = titles;
            }
            else if(recMethod == 2){
                userIds = indices;
                userTitle = titles;
            }
            else if(recMethod == 3){
                popIds = indices;
                popTitle = titles;
            }
        }
        catch(Exception e)
        {}
    
    }
    
    public static void writeCSV(int userID, int movieID, float rating) throws IOException{
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    FileWriter fw = new FileWriter("data/usuarios.csv",true);
    StringBuilder sb = new StringBuilder();
    
    sb.append(userID);
    sb.append(',');
    sb.append(movieID);
    sb.append(',');
    sb.append(rating);
    sb.append(',');
    sb.append(timestamp.getTime());
    sb.append('\n');
    fw.append(sb.toString());
    fw.close();
    System.out.println("done!");
}
 
}