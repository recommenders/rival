package net.recommenders.rival.examples.mdp;

import java.util.HashMap;
import java.util.Map;

public class EvaluationMetrics {

   private Double rmse;
   private Double mae;
   private Double precision;
   private Double recall;
   private Double ndcg;

   private Map<Integer, Double> precisionAtK;
   private Map<Integer, Double> recallAtK;
   private Map<Integer, Double> ndcgAtK;

   public Double getRmse() {
      return rmse;
   }

   public Double getMae() {
      return mae;
   }

   public Double getPrecision() {
      return precision;
   }

   public Double getRecall() {
      return recall;
   }

   public Double getNdcg() {
      return ndcg;
   }

   public Double getPrecisionAtK(int cutoff) {
      if (precisionAtK.containsKey(cutoff))
         return precisionAtK.get(cutoff);
      return Double.NaN;
   }

   public Double getRecallAtK(int cutoff) {
      if (recallAtK.containsKey(cutoff))
         return recallAtK.get(cutoff);
      return Double.NaN;
   }

   public Double getNdcgAtK(int cutoff) {
      if (ndcgAtK.containsKey(cutoff))
         return ndcgAtK.get(cutoff);
      return Double.NaN;
   }

   public EvaluationMetrics() {
      this.rmse = 0d;
      this.mae = 0d;
      this.precision = 0d;
      this.recall = 0d;
      this.ndcg = 0d;
      this.precisionAtK = new HashMap<>();
      this.recallAtK = new HashMap<>();
      this.ndcgAtK = new HashMap<>();
   }

   public void setRMSE(Double rmse) {
      this.rmse = rmse;
   }

   public void setMAE(Double mae) {
      this.mae = mae;
   }

   public void setPrecisionAtK(int cutoff, Double precision) {
      this.precisionAtK.put(cutoff, precision);
   }

   public void setRecallAtK(int cutoff, Double recall) {
      this.recallAtK.put(cutoff, recall);
   }

   public void setNDCGAtK(int cutoff, Double ndcg) {
      this.ndcgAtK.put(cutoff, ndcg);
   }
}
