package net.recommenders.rival.split.splitter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
//import java.util.Set;

//import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.core.DataModelIF;

public class CrossValidationSplitterIterative<U, I>  extends CrossValidationSplitter<U, I> 
{

	private String outPath;

	public CrossValidationSplitterIterative(int nFold, boolean perUsers, long seed, String outPath) 
	{
		super(nFold, perUsers, seed);
		this.outPath = outPath;
	}

	 /**
     * {@inheritDoc}
     */
    @Override
    public DataModelIF<U, I>[] split(final DataModelIF<U, I> data)  
    {
    	try 
    	{
	        File dir = new File(outPath);
	        if (!dir.exists()) {
	            dir.mkdir();
	        }
	        final FileWriter[] splits = new FileWriter[2 * nFolds];
	        for (int i = 0; i < nFolds; i++) 
	        {
	        	String 	trainingFile = outPath+"train_"+i+".csv",
	        			testFile 	 = outPath+"test_"+i+".csv";
	        	splits[2 * i] 		= new FileWriter(trainingFile);
	            splits[2 * i + 1] 	= new FileWriter(testFile);
	            
	        }
	        
	        if (perUser) 
	        {
	            int n = 0;
	            for (U user : data.getUsers()) {
	                List<I> items = new ArrayList<I>(data.getUserItemPreferences().get(user).keySet());
	                Collections.shuffle(items, rnd);
	                for (I item : items) {
	                    Double pref = data.getUserItemPreferences().get(user).get(item);
//	                    Set<Long> time = null;
//	                    if (data.getgetUserItemTimestamps().containsKey(user) && data.getUserItemTimestamps().get(user).containsKey(item)) {
//	                        time = data.getUserItemTimestamps().get(user).get(item);
//	                    }
	                    int curFold = n % nFolds;
	                    for (int i = 0; i < nFolds; i++) {
	                    	FileWriter f_writer = splits[2 * i]; // training
	                        if (i == curFold) {
	                        	f_writer = splits[2 * i + 1]; // test
	                        }
	                        
	                        if(f_writer==null) continue; // not a "valid" fold already computed
	                        
	                        if (pref != null) {
	                        	f_writer.write(user+"\t"+item+"\t"+pref);
	                        }
//	                        if (time != null) {
//	                            for (Long t : time) {
//	                            	f_writer.write("\t"+t);
//	                            }
//	                        }
	                        f_writer.write("\n");
	                        f_writer.flush();
	                    }
	                    n++;
	                }
	            }
	        } 
	        else {
	            List<U> users = new ArrayList<U>(data.getUsers());
	            Collections.shuffle(users, rnd);
	            int n = 0;
	            for (U user : users) 
	            {
	                List<I> items = new ArrayList<I>(data.getUserItemPreferences().get(user).keySet());
	                Collections.shuffle(items, rnd);
	                for (I item : items) {
	                    Double pref = data.getUserItemPreferences().get(user).get(item);
//	                    Set<Long> time = null;
//	                    if (data.getUserItemTimestamps().containsKey(user) && data.getUserItemTimestamps().get(user).containsKey(item)) {
//	                        time = data.getUserItemTimestamps().get(user).get(item);
//	                    }
	                    int curFold = n % nFolds;
	                    for (int i = 0; i < nFolds; i++) 
	                    {
	                    	FileWriter f_writer = splits[2 * i]; // training
	                        if (i == curFold) f_writer = splits[2 * i + 1]; // test
	                        
	                        if(f_writer==null) continue; // not a "valid" fold already computed
	                        
	                        
	                        if (pref != null) {
	                        	f_writer.write(user+"\t"+item+"\t"+pref);
	                        }
//	                        if (time != null) {
//	                            for (Long t : time) {
//	                            	f_writer.write("\t"+t);
//	                            }
//	                        }
							f_writer.write("\n");
							f_writer.flush();
	                    }
	                    n++;
	                }
	            }
	        }
	        
	        // Close files
	        for (int i = 0; i < nFolds; i++) 
	        {
	            splits[2 * i].close();
	            splits[2 * i + 1].close();
	        }
	        
	    } catch (IOException e) {e.printStackTrace();}
        return null;
    }
}