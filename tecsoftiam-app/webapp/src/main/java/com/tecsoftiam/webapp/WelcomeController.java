package com.tecsoftiam.webapp;

import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.DirectoryRole;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.User;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
public class WelcomeController {

    
    UserRepository userRepo;
    // inject via application.properties
  

   @GetMapping("/index")
   public String index(Model model) throws IOException {
      
       return "index";
   }


   @GetMapping("/users/{id}")
    public String userdetails(@PathVariable(value = "id") String userId ,Model model ) throws IOException {
        Graph msGraph= new Graph();
       User users= msGraph.getAdUser(userId);
       model.addAttribute("users", users);
       List<DirectoryRole> roles= msGraph.GetAllRoleFrom(userId);
       model.addAttribute("roleList", roles);
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
        List<Group>  groups= msGraph.getGroups();
       model.addAttribute("groups", groups);
        return "groups";
    }
 

    @GetMapping("/createUser")
    public String userForm(Model model) throws IOException {
        adUser user = new adUser();
        Graph msGraph= new Graph();
        model.addAttribute("user", user);
        List<DirectoryRole> roles= msGraph.getDirectoryRoles();
        model.addAttribute("roles", roles);
         
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
            System.out.println(user.roles.get(i));
        }
        return "index";
    }

    @PostMapping("/deleteUser")
    public String deleteUser(@RequestParam Map<String, String> requestParams) throws IOException{
        Graph msGraph= new Graph();
        msGraph.deleteUser(requestParams.get("id"));
        return "redirect:/users";
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

}