package com.library.controller;

import com.library.Main;
import com.library.dao.LibraryDAO;
import com.library.model.*;
import com.library.util.Dialogs;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.util.List;

public class DashboardController {
 private final LibraryDAO dao=new LibraryDAO();
 @javafx.fxml.FXML private BorderPane root;
 private Label pageHeading;
 private VBox content;

 public void initialize(){
  VBox sidebar=new VBox(9);sidebar.getStyleClass().add("sidebar");sidebar.setPrefWidth(220);
  Label brand=new Label("BOOKNEST");brand.getStyleClass().add("brand");
  Label sub=new Label("LIBRARY MANAGEMENT");sub.getStyleClass().add("sidebar-subtitle");
  sidebar.getChildren().addAll(brand,sub,new Separator());
  String[] pages={"Dashboard","Add Book","Add Member","Manage Members","Issue Book","Return Book","Borrowing History"};
  for(String page:pages){
   Button b=new Button(page);b.getStyleClass().add("nav-button");b.setMaxWidth(Double.MAX_VALUE);
   b.setOnAction(e->show(page));sidebar.getChildren().add(b);
  }
  Region spacer=new Region();VBox.setVgrow(spacer,Priority.ALWAYS);sidebar.getChildren().add(spacer);
  Button logout=new Button("↪  Logout");logout.getStyleClass().add("nav-button");logout.setMaxWidth(Double.MAX_VALUE);
  logout.setOnAction(e->{try{Main.showLogin();}catch(Exception ex){error(ex);}});
  sidebar.getChildren().add(logout);root.setLeft(sidebar);

  VBox top=new VBox();top.getStyleClass().add("topbar");
  pageHeading=new Label("Dashboard");pageHeading.getStyleClass().add("page-title");top.getChildren().add(pageHeading);root.setTop(top);
  content=new VBox(18);content.getStyleClass().add("content");
  ScrollPane scroll=new ScrollPane(content);scroll.setFitToWidth(true);
  scroll.setStyle("-fx-background: #f4f7fb; -fx-background-color: #f4f7fb;");
  root.setCenter(scroll);show("Dashboard");
 }
 private void show(String page){
  pageHeading.setText(page);content.getChildren().clear();
  try{switch(page){
   case "Dashboard"->dashboard();case "Add Book"->addBookForm();case "Add Member"->addMemberForm();
   case "Manage Members"->membersPage();case "Issue Book"->issuePage();
   case "Return Book"->returnPage();case "Borrowing History"->historyPage();
  }}catch(Exception e){error(e);}
 }
 private void dashboard() throws Exception {
  HBox cards=new HBox(14);
  cards.getChildren().addAll(stat("Total Books",dao.count("books")),stat("Total Members",dao.count("members")),
   stat("Currently Borrowed",dao.borrowedCount()),stat("Overdue Books",dao.overdueCount()));
  content.getChildren().addAll(cards,section("Welcome to BookNest","Use the navigation menu to manage books, members, issuing, returns and borrowing history."));
 }
 private VBox stat(String name,int value){
  VBox b=new VBox(8);b.getStyleClass().add("stat-card");b.setPrefWidth(220);
  Label n=new Label(String.valueOf(value));n.getStyleClass().add("stat-number");
  Label l=new Label(name);l.getStyleClass().add("stat-label");b.getChildren().addAll(n,l);
  HBox.setHgrow(b,Priority.ALWAYS);return b;
 }
 private VBox section(String title,String message){
  VBox b=new VBox(10);b.getStyleClass().add("form-card");
  Label t=new Label(title);t.getStyleClass().add("section-title");b.getChildren().addAll(t,new Label(message));return b;
 }
 private TextField field(String prompt){TextField t=new TextField();t.setPromptText(prompt);return t;}
 private Button primary(String text,Runnable action){
  Button b=new Button(text);b.getStyleClass().add("primary-button");b.setOnAction(e->action.run());return b;
 }
 private Button secondary(String text,Runnable action){
  Button b=new Button(text);b.getStyleClass().add("secondary-button");b.setOnAction(e->action.run());return b;
 }
 private void form(String title,Node... nodes){
  VBox box=new VBox(12);box.getStyleClass().add("form-card");box.setMaxWidth(780);
  Label t=new Label(title);t.getStyleClass().add("section-title");box.getChildren().add(t);
  box.getChildren().addAll(nodes);content.getChildren().add(box);
 }
 private void addBookForm(){
  TextField isbn=field("Book ID / ISBN"),title=field("Book title"),author=field("Author"),
   category=field("Category"),year=field("Published year"),qty=field("Quantity");
  Button save=primary("Add Book",()->{
   try{
    if(blank(isbn,title,author,category,year,qty))return;
    int y=Integer.parseInt(year.getText()),q=Integer.parseInt(qty.getText());
    if(y<0||q<1)throw new IllegalArgumentException("Year must be zero or greater and quantity must be at least 1.");
    dao.addBook(isbn.getText().trim(),title.getText().trim(),author.getText().trim(),
     category.getText().trim(),y,q);
    Dialogs.info("Success","Book added successfully.");show("Add Book");
   }catch(Exception e){error(e);}
  });
  Button clear=secondary("Clear",()->{isbn.clear();title.clear();author.clear();category.clear();year.clear();qty.clear();});
  form("Book information",label("Book ID / ISBN"),isbn,label("Book Title"),title,label("Author"),author,
   label("Category"),category,label("Published Year"),year,label("Quantity"),qty,new HBox(10,save,clear));
 }
 private void addMemberForm(){
  TextField code=field("Member ID"),name=field("Full name"),email=field("Email"),
   phone=field("Phone number"),address=field("Address");
  Button save=primary("Register Member",()->{
   try{
    if(blank(code,name,email,phone,address))return;
    if(!email.getText().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
     throw new IllegalArgumentException("Enter a valid email address.");
    dao.addMember(code.getText().trim(),name.getText().trim(),email.getText().trim(),
     phone.getText().trim(),address.getText().trim());
    Dialogs.info("Success","Member registered successfully.");show("Add Member");
   }catch(Exception e){error(e);}
  });
  form("Member information",label("Member ID"),code,label("Full Name"),name,label("Email"),email,
   label("Phone Number"),phone,label("Address"),address,save);
 }
 private void membersPage() throws Exception {
  TextField search=field("Search by member ID, name or email");
  TableView<Member> table=memberTable();
  Runnable reload=()->{try{table.setItems(FXCollections.observableArrayList(dao.members(search.getText())));}catch(Exception e){error(e);}};
  search.textProperty().addListener((o,a,b)->reload.run());reload.run();
  TextField code=field("Member ID"),name=field("Full name"),email=field("Email"),phone=field("Phone"),address=field("Address");
  Button edit=primary("Save selected member",()->{
   Member m=table.getSelectionModel().getSelectedItem();
   if(m==null){Dialogs.warning("Select member","Choose a member from the table.");return;}
   try{if(blank(code,name,email,phone,address))return;
    dao.updateMember(m.id,code.getText(),name.getText(),email.getText(),phone.getText(),address.getText());
    reload.run();Dialogs.info("Updated","Member details updated.");
   }catch(Exception e){error(e);}
  });
  Button delete=secondary("Delete selected",()->{
   Member m=table.getSelectionModel().getSelectedItem();
   if(m==null){Dialogs.warning("Select member","Choose a member first.");return;}
   if(Dialogs.confirm("Confirm delete","Delete "+m.name+"?"))try{dao.deleteMember(m.id);reload.run();}
   catch(Exception e){error(e);}
  });
  table.getSelectionModel().selectedItemProperty().addListener((o,a,m)->{
   if(m!=null){code.setText(m.code);name.setText(m.name);email.setText(m.email);phone.setText(m.phone);address.setText(m.address);}
  });
  form("Registered members",search,table,label("Edit selected member"),code,name,email,phone,address,new HBox(10,edit,delete));
  table.setPrefHeight(300);
 }
 private TableView<Member> memberTable(){
  TableView<Member> t=new TableView<>();
  t.getColumns().add(col("Member ID",m->m.code));t.getColumns().add(col("Full Name",m->m.name));
  t.getColumns().add(col("Email",m->m.email));t.getColumns().add(col("Phone",m->m.phone));
  t.getColumns().add(col("Address",m->m.address));return t;
 }
 private void issuePage() throws Exception {
  ComboBox<Member> member=new ComboBox<>(FXCollections.observableArrayList(dao.members("")));
  member.setPromptText("Select member");member.setMaxWidth(Double.MAX_VALUE);
  ComboBox<Book> book=new ComboBox<>(FXCollections.observableArrayList(dao.books().stream().filter(b->b.available>0).toList()));
  book.setPromptText("Select available book");book.setMaxWidth(Double.MAX_VALUE);
  DatePicker issue=new DatePicker(LocalDate.now()),due=new DatePicker(LocalDate.now().plusDays(14));
  issue.setMaxWidth(Double.MAX_VALUE);due.setMaxWidth(Double.MAX_VALUE);
  Button save=primary("Issue Book",()->{
   try{
    if(member.getValue()==null||book.getValue()==null||issue.getValue()==null||due.getValue()==null)
     throw new IllegalArgumentException("Select a member, book, issue date and due date.");
    dao.issue(member.getValue().id,book.getValue().id,issue.getValue(),due.getValue());
    Dialogs.info("Success","Book issued successfully.");show("Issue Book");
   }catch(Exception e){error(e);}
  });
  form("Issue a book",label("Select member"),member,label("Select book"),book,
   label("Issue date"),issue,label("Due date"),due,save);
 }
 private void returnPage() throws Exception {
  List<Borrowing> open=dao.borrowings().stream().filter(b->b.returnDate==null).toList();
  ComboBox<Borrowing> record=new ComboBox<>(FXCollections.observableArrayList(open));
  record.setPromptText("Select borrowed book");record.setMaxWidth(Double.MAX_VALUE);
  DatePicker date=new DatePicker(LocalDate.now());date.setMaxWidth(Double.MAX_VALUE);
  Label details=label("Choose a borrowing record to see member and due-date information.");
  record.setOnAction(e->{Borrowing b=record.getValue();if(b!=null)details.setText(
   "Member: "+b.memberName+" ("+b.memberCode+") | Borrowed: "+b.issueDate+" | Due: "+b.dueDate+
   " | "+(b.dueDate.isBefore(LocalDate.now())?"OVERDUE":"On time"));});
  Button ret=primary("Return Book",()->{
   try{
    if(record.getValue()==null||date.getValue()==null)throw new IllegalArgumentException("Select a borrowed book and return date.");
    if(Dialogs.confirm("Confirm return","Mark this book as returned?")){
     dao.returnBook(record.getValue().id,date.getValue());Dialogs.info("Returned","Book return recorded.");show("Return Book");
    }
   }catch(Exception e){error(e);}
  });
  form("Return a borrowed book",label("Borrowed book"),record,details,label("Return date"),date,ret);
 }
 private void historyPage() throws Exception {
  TableView<Borrowing> t=borrowingTable(dao.borrowings());t.setPrefHeight(500);
  form("Borrowing history",t);
 }
 private TableView<Borrowing> borrowingTable(List<Borrowing> list){
  TableView<Borrowing> t=new TableView<>(FXCollections.observableArrayList(list));
  t.getColumns().add(col("Member ID",b->b.memberCode));t.getColumns().add(col("Book Title",b->b.title));
  t.getColumns().add(col("Issue Date",b->b.issueDate.toString()));t.getColumns().add(col("Due Date",b->b.dueDate.toString()));
  t.getColumns().add(col("Return Date",b->b.returnDate==null?"—":b.returnDate.toString()));
  t.getColumns().add(col("Status",Borrowing::displayStatus));return t;
 }
 private <T> TableColumn<T,String> col(String title,java.util.function.Function<T,String> fn){
  TableColumn<T,String> c=new TableColumn<>(title);
  c.setCellValueFactory(x->new SimpleStringProperty(fn.apply(x.getValue())));c.setPrefWidth(145);return c;
 }
 private Label label(String text){return new Label(text);}
 private boolean blank(TextField... fields){
  for(TextField f:fields)if(f.getText().trim().isEmpty()){
   Dialogs.warning("Missing information","Please complete all fields.");return true;
  }return false;
 }
 private void error(Exception e){
  e.printStackTrace();Dialogs.error("Operation failed",e.getMessage()==null?"An unexpected error occurred.":e.getMessage());
 }
}
