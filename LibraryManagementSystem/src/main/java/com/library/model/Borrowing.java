package com.library.model;
import java.time.LocalDate;
public class Borrowing {
 public final int id,memberId,bookId;
 public final String memberCode,memberName,isbn,title,status;
 public final LocalDate issueDate,dueDate,returnDate;
 public Borrowing(int id,int memberId,int bookId,String memberCode,String memberName,String isbn,String title,
                  LocalDate issueDate,LocalDate dueDate,LocalDate returnDate,String status){
  this.id=id;this.memberId=memberId;this.bookId=bookId;this.memberCode=memberCode;this.memberName=memberName;
  this.isbn=isbn;this.title=title;this.issueDate=issueDate;this.dueDate=dueDate;this.returnDate=returnDate;this.status=status;
 }
 public String displayStatus(){
  return returnDate==null && dueDate.isBefore(LocalDate.now()) ? "OVERDUE" : status;
 }
 @Override public String toString(){return title+" — "+memberName+" (due "+dueDate+")";}
}
