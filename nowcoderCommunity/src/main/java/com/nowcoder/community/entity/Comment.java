package com.nowcoder.community.entity;



import java.lang.reflect.Type;
import java.util.Date;

public class Comment {

   private int id;
   private int userId;
   private int entityId;
   private int entityType;
   private int targetId;
   private String content;
   private int status;
   private Date createTime;


   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public int getUserId() {
      return userId;
   }

   public void setUserId(int userId) {
      this.userId = userId;
   }

   public int getEntityId() {
      return entityId;
   }

   public void setEntityId(int entityId) {
      this.entityId = entityId;
   }

   public int getEntityType() {
      return entityType;
   }

   public void setEntityType(int entityType) {
      this.entityType = entityType;
   }

   public int getTargetId() {
      return targetId;
   }

   public void setTargetId(int targetId) {
      this.targetId = targetId;
   }

   public String getContent() {
      return content;
   }

   public void setContent(String content) {
      this.content = content;
   }

   public int getStatus() {
      return status;
   }

   public void setStatus(int status) {
      this.status = status;
   }

   public Date getCreateTime() {
      return createTime;
   }

   public void setCreateTime(Date creaTime) {
      this.createTime = creaTime;
   }

   @Override
   public String toString() {
      return "Comment{" +
            "id=" + id +
            ", userId=" + userId +
            ", entityId=" + entityId +
            ", targetId=" + targetId +
            ", content='" + content + '\'' +
            ", status=" + status +
            ", creaTime=" + createTime +
            '}';
   }
}
