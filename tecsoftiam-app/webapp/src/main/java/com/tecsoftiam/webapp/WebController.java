package com.tecsoftiam.webapp;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.Size;

import com.azure.core.annotation.Post;
import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.DirectoryRole;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.User;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for web app
 */
@Controller
public class WebController {

    
    UserRepository userRepo;
    // inject via application.properties
  

   @GetMapping("/index")
   public String index(Model model) throws IOException {
       Scope scope=new Scope();
      model.addAttribute("scopeName", scope.getScopeName());
       return "index";
   }

   @GetMapping("/users/{id}")
    public String userdetails(@PathVariable(value = "id") String userId ,Model model ) throws IOException {
        Graph msGraph= new Graph();
       User users= msGraph.getAdUser(userId);
       model.addAttribute("users", users);
       List<DirectoryRole> roles= msGraph.GetAllRoleFrom(userId);
       model.addAttribute("roleList", roles);
       List<Group> group= msGraph.groupsOf(userId);
       model.addAttribute("groupList", group);
        return "userDetail";
    }

   @GetMapping("/users")
    public String user(Model model) throws IOException {
        Graph msGraph= new Graph();
       List<User> users= msGraph.getAdUserList();
       model.addAttribute("users", users);
        return "user";
    }

    @GetMapping("/roles")
    public String roles(Model model) throws IOException {
        Graph msGraph= new Graph();
        List<DirectoryRole>  roles= msGraph.getDirectoryRoles();
       model.addAttribute("roles", roles);
        return "roles";
    }

    @GetMapping("/roles/{id}")
    public String rolesdetails(@PathVariable(value = "id") String templateId ,Model model ) throws IOException {
        Graph msGraph= new Graph();
       List<DirectoryObject> lst= msGraph.getUserRoles(templateId);
       DirectoryRole role= msGraph.getRoleDetails(templateId);
       model.addAttribute("users", lst);
       model.addAttribute("role", role);
        return "roledetails";
    }

    @GetMapping("/groups")
    public String groups(Model model) throws IOException {
        Graph msGraph= new Graph();
        List<Group>  groups= msGraph.getGroupsList();
       model.addAttribute("groups", groups);
        return "groups";
    }
    @GetMapping("/groups/{id}")
    public String groupsDetails(@PathVariable(value = "id") String groupId ,Model model ) throws IOException {
        Graph msGraph= new Graph();
       List<DirectoryObject> lst= msGraph.usersInGroup(groupId);
       Group group= msGraph.groupDetail(groupId);
       model.addAttribute("users", lst);
       model.addAttribute("group", group);
        return "groupDetails";
    }
 

    @GetMapping("/createUser")
    public String userForm(Model model) throws IOException {
        adUser user = new adUser();
        Graph msGraph= new Graph();
        model.addAttribute("user", user);
        List<DirectoryRole> roles= msGraph.getDirectoryRoles();
        model.addAttribute("roles", roles);
        List<Group> groups= msGraph.getGroupsList();
        model.addAttribute("groups", groups);
        return "createUser";
    }

        @PostMapping("/createUser")
    public String createUser(Model Model, @ModelAttribute("user") adUser user ) throws IOException {
        Graph msGraph= new Graph();
        String id;
        msGraph.CreateUser(user.getDisplayName(), user.getNickName(), user.getMail(), user.getName(), user.getPassword());
        id=msGraph.getAdUserByDP(user.getDisplayName()).id;
        
        for(int i=0 ; i<user.roles.size() ; i++){
            msGraph.grantRole(user.roles.get(i), id);
           
        }
        for(int i=0 ; i<user.groups.size() ; i++){
            msGraph.addToGroup(id, user.groups.get(i));
           
        }
        return "index";
    }

    @PostMapping("/deleteUser")
    public String deleteUser(@RequestParam Map<String, String> requestParams) throws IOException{
        Graph msGraph= new Graph();

        msGraph.deleteUser(requestParams.get("id"));
        return "redirect:/users";
    }

    @GetMapping("/giveGroup/{id}")
    public String giveGroup(@PathVariable(value = "id") String id ,Model model) throws IOException {
        Graph msGraph= new Graph();
        User user = msGraph.getAdUser(id);
        adUser aduser= new adUser();
        Set<Group> groups= msGraph.NotHaveGroupList(id);
        model.addAttribute("user", user);
        model.addAttribute("groups", groups);
        model.addAttribute("aduser", aduser);
        return "giveGroup";
    }
    @PostMapping("/giveGroup{id}")
    public String giveGroupP(@RequestParam Map<String, String> requestParams, @ModelAttribute("user") adUser user,Model Model ) throws IOException {
        Graph msGraph= new Graph();
        String id= requestParams.get("id");
        for(int i=0 ; i<user.groups.size() ; i++){
            msGraph.addToGroup( id, user.groups.get(i));
        }
        return "redirect:/users/"+id;
    }
    @GetMapping("/deleteGroup/{id}")
    public String deleteGroup(@PathVariable(value = "id") String id ,Model model) throws IOException {
        Graph msGraph= new Graph();
        User user = msGraph.getAdUser(id);
        adUser aduser= new adUser();
        List<Group> groups= msGraph.groupsOf(id);
        model.addAttribute("user", user);
        model.addAttribute("groups", groups);
        model.addAttribute("aduser", aduser);
        return "deleteGroup";
    }
    @PostMapping("/deleteGroup{id}")
    public String deleteGroupP(@RequestParam Map<String, String> requestParams, @ModelAttribute("user") adUser user,Model Model ) throws IOException {
        Graph msGraph= new Graph();
        String id= requestParams.get("id");
        for(int i=0 ; i<user.groups.size() ; i++){
            msGraph.deleteFromGroup(id,user.groups.get(i));
        }
        return "redirect:/users/"+id;
    }

    @GetMapping("/giveRole/{id}")
    public String giveRole(@PathVariable(value = "id") String id ,Model model) throws IOException {
        Graph msGraph= new Graph();
        User user = msGraph.getAdUser(id);
        adUser aduser= new adUser();
        Set<DirectoryRole> roles= msGraph.NotHaveRoleList(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        model.addAttribute("aduser", aduser);
        return "giveRole";
    }
    @PostMapping("/giveRole{id}")
    public String giveRoleP(@RequestParam Map<String, String> requestParams, @ModelAttribute("user") adUser user,Model Model ) throws IOException {
        Graph msGraph= new Graph();
        String id= requestParams.get("id");
        for(int i=0 ; i<user.roles.size() ; i++){
            msGraph.grantRole(user.roles.get(i), id);
        }
        return "redirect:/users/"+id;
    }
    @GetMapping("/deleteRole/{id}")
    public String deleteRole(@PathVariable(value = "id") String id ,Model model) throws IOException {
        Graph msGraph= new Graph();
        User user = msGraph.getAdUser(id);
        adUser aduser= new adUser();
        List<DirectoryRole> roles= msGraph.GetAllRoleFrom(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        model.addAttribute("aduser", aduser);
        return "deleteRole";
    }
    @PostMapping("/deleteRole{id}")
    public String deleteRoleP(@RequestParam Map<String, String> requestParams, @ModelAttribute("user") adUser user,Model Model ) throws IOException {
        Graph msGraph= new Graph();
        String id= requestParams.get("id");
        for(int i=0 ; i<user.roles.size() ; i++){
            msGraph.deleteRoleFrom(user.roles.get(i), id);
        }
        return "redirect:/users/"+id;
    }
    @GetMapping("/scopes")
    public String scopePage(Model model) throws IOException, SQLException{
        dbConnect db= new dbConnect();
        Scope scope=new Scope();
        model.addAttribute("selectedScope", scope);
        List<Scope> scopeList= db.getScopeList();
        model.addAttribute("scopes", scopeList);
        return "scopes";
    }
    @PostMapping("/scopes")
    public String scopePageP(Model Model, @ModelAttribute("selectedScope") Scope scope) throws IOException, SQLException{
        
        dbConnect db= new dbConnect();
        Scope newScope=db.getScope(scope.getScopeName());
        newScope.writeInFile();
        return "redirect:/index";
    }
    @GetMapping("/addScope")
    public String addScopeFrom(Model model) throws IOException{
        Scope scope= new Scope(0);
        model.addAttribute("scope", scope);
        return "addScope";
    }
    @PostMapping("/addScope")
    public String addScopeP(Model model, @ModelAttribute("scope") Scope scope) throws IOException, SQLException{   
        dbConnect db= new dbConnect();
        db.addScope(scope.getTenantId(), scope.getPassword(), scope.getScopeName());
        return "redirect:/scopes";
    }
    @GetMapping("/adChanges")
    public String adChanges(Model model) throws IOException, SQLException{
       changeDetect detect=new changeDetect();
       List<adChanges> changes= new ArrayList<adChanges>();
       changes= detect.changesList();
       changesWrapper wrap=new changesWrapper();
       wrap.setChangesList(changes);
        model.addAttribute("wrapper", wrap);
        return "adChanges";
    }
    @PostMapping("/adChanges")
    public String adChangesP(Model model ,@ModelAttribute("wrapper") changesWrapper Viewlist, BindingResult bindingResult) throws IOException, SQLException{
        changeDetect detect=new changeDetect();
        List<adChanges> list=new ArrayList<adChanges>();
        list= Viewlist.getChangesList();
        detect.applyAllChanges(list);
        return "redirect:/adChanges";
    }
    @GetMapping("/history")
    public String history(Model model) throws IOException, SQLException{
        dbConnect db= new dbConnect();
        List<history> list=new ArrayList<history>();
        list=db.getHistoryList();
        model.addAttribute("list", list);
        return "history";
    }
    @GetMapping("/history/{id}")
    public String historyDetail(@PathVariable(value = "id") String id , Model model) throws IOException, SQLException{
        dbConnect db= new dbConnect();
        List<adChanges> list=new ArrayList<adChanges>();
        list=db.getChangesList(Integer.parseInt(id));
        model.addAttribute("list", list);
        model.addAttribute("id", id);
        return "historyDetail";
    }
    @PostMapping("/deleteHistory")
    public String deleteHistory(@RequestParam Map<String, String> requestParams) throws IOException, NumberFormatException, SQLException{
       dbConnect db=new dbConnect();
        db.deleteHistory(Integer.parseInt(requestParams.get("id")));
        return "redirect:/history";
    }
}