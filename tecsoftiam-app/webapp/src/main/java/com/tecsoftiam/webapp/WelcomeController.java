package com.tecsoftiam.webapp;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.DirectoryRole;
import com.microsoft.graph.models.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
    @Value("${welcome.message:test}")
    private String message = "Hello World";

    @RequestMapping("/")
    public String welcome(Map<String, Object> model) {
        model.put("message", this.message);
        return "welcome";
    }
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new AppUser());
        
        return "signup_form";
    }
    @PostMapping("/process_register")
public String processRegister(AppUser user) {
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    String encodedPassword = passwordEncoder.encode(user.getPassword());
    user.setPassword(encodedPassword);
     
    userRepo.save(user);
     
    return "register_success";
    }

    
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
 

    @GetMapping("/welcome")
    public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name,
            Model model) {
        model.addAttribute("name", name);
        return "greeting";
    }

}