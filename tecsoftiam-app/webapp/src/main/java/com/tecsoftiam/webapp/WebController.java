package com.tecsoftiam.webapp;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.DirectoryRole;
import com.microsoft.graph.models.Domain;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for web app 
 * Author: Deryck Olivier
 */
@Controller
public class WebController {

    UserRepository userRepo;

    
    /** 
     * @param model
     * @return String
     * @throws IOException
     */
    @GetMapping("/index")
    public String index(Model model) throws IOException {
        Scope scope = new Scope();
        model.addAttribute("scopeName", scope.getScopeName());
        return "index";
    }

    
    /** 
     * @param userId
     * @param model
     * @return String
     * @throws IOException
     */
    @GetMapping("/users/{id}")
    public String userdetails(@PathVariable(value = "id") String userId, Model model) throws IOException {
        Graph msGraph = new Graph();
        User users = msGraph.getAdUser(userId);
        model.addAttribute("users", users);
        List<DirectoryRole> roles = msGraph.GetAllRoleFrom(userId);
        model.addAttribute("roleList", roles);
        List<Group> group = msGraph.groupsOf(userId);
        model.addAttribute("groupList", group);
        return "userDetail";
    }

    
    /** 
     * @param model
     * @return String
     * @throws IOException
     */
    @GetMapping("/users")
    public String user(Model model) throws IOException {
        Graph msGraph = new Graph();
        List<User> users = msGraph.getAdUserList();
        model.addAttribute("users", users);
        return "user";
    }

    
    /** 
     * @param model
     * @return String
     * @throws IOException
     */
    @GetMapping("/roles")
    public String roles(Model model) throws IOException {
        Graph msGraph = new Graph();
        List<DirectoryRole> roles = msGraph.getDirectoryRoles();
        model.addAttribute("roles", roles);
        return "roles";
    }

    
    /** 
     * @param templateId
     * @param model
     * @return String
     * @throws IOException
     */
    @GetMapping("/roles/{id}")
    public String rolesdetails(@PathVariable(value = "id") String templateId, Model model) throws IOException {
        Graph msGraph = new Graph();
        List<DirectoryObject> lst = msGraph.getUserRoles(templateId);
        DirectoryRole role = msGraph.getRoleDetails(templateId);
        model.addAttribute("users", lst);
        model.addAttribute("role", role);
        return "roledetails";
    }

    
    /** 
     * @param model
     * @return String
     * @throws IOException
     */
    @GetMapping("/groups")
    public String groups(Model model) throws IOException {
        Graph msGraph = new Graph();
        List<Group> groups = msGraph.getGroupsList();
        model.addAttribute("groups", groups);
        return "groups";
    }

    
    /** 
     * @param groupId
     * @param model
     * @return String
     * @throws IOException
     */
    @GetMapping("/groups/{id}")
    public String groupsDetails(@PathVariable(value = "id") String groupId, Model model) throws IOException {
        Graph msGraph = new Graph();
        List<DirectoryObject> lst = msGraph.usersInGroup(groupId);
        Group group = msGraph.groupDetail(groupId);
        model.addAttribute("users", lst);
        model.addAttribute("group", group);
        return "groupDetails";
    }

    
    /** 
     * @param model
     * @return String
     * @throws IOException
     */
    @GetMapping("/createUser")
    public String userForm(Model model) throws IOException {
        AdUser user = new AdUser();
        Graph msGraph = new Graph();
        model.addAttribute("user", user);
        List<DirectoryRole> roles = msGraph.getDirectoryRoles();
        model.addAttribute("roles", roles);
        List<Group> groups = msGraph.getGroupsList();
        model.addAttribute("groups", groups);
        List<Domain> domains= msGraph.domainList();
        model.addAttribute("domains", domains);
        return "createUser";
    }

    
    /** 
     * @param Model
     * @param user
     * @return String
     * @throws IOException
     */
    @PostMapping("/createUser")
    public String createUser(Model Model, @ModelAttribute("user") AdUser user) throws IOException {
        Graph msGraph = new Graph();
        String id;
        String nick=user.getNickName();
        String mail= nick+"@"+user.getDomain();
        msGraph.CreateUser(user.getDisplayName(), nick, mail, user.getName(),
                user.getPassword());
        id = msGraph.getAdUserByDP(user.getDisplayName()).id;

        for (int i = 0; i < user.getRoles().size(); i++) {
            msGraph.grantRole(user.getRoles().get(i), id);

        }
        for (int i = 0; i < user.getGroups().size(); i++) {
            msGraph.addToGroup(id, user.getGroups().get(i));

        }
        return "index";
    }

    
    /** 
     * @param requestParams
     * @return String
     * @throws IOException
     */
    @PostMapping("/deleteUser")
    public String deleteUser(@RequestParam Map<String, String> requestParams) throws IOException {
        Graph msGraph = new Graph();

        msGraph.deleteUser(requestParams.get("id"));
        return "redirect:/users";
    }

    
    /** 
     * @param id
     * @param model
     * @return String
     * @throws IOException
     */
    @GetMapping("/giveGroup/{id}")
    public String giveGroup(@PathVariable(value = "id") String id, Model model) throws IOException {
        Graph msGraph = new Graph();
        User user = msGraph.getAdUser(id);
        AdUser aduser = new AdUser();
        Set<Group> groups = msGraph.NotHaveGroupList(id);
        model.addAttribute("user", user);
        model.addAttribute("groups", groups);
        model.addAttribute("aduser", aduser);
        return "giveGroup";
    }

    
    /** 
     * @param requestParams
     * @param user
     * @param Model
     * @return String
     * @throws IOException
     */
    @PostMapping("/giveGroup{id}")
    public String giveGroupP(@RequestParam Map<String, String> requestParams, @ModelAttribute("user") AdUser user,
            Model Model) throws IOException {
        Graph msGraph = new Graph();
        String id = requestParams.get("id");
        for (int i = 0; i < user.getGroups().size(); i++) {
            msGraph.addToGroup(id, user.getGroups().get(i));
        }
        return "redirect:/users/" + id;
    }

    
    /** 
     * @param id
     * @param model
     * @return String
     * @throws IOException
     */
    @GetMapping("/deleteGroup/{id}")
    public String deleteGroup(@PathVariable(value = "id") String id, Model model) throws IOException {
        Graph msGraph = new Graph();
        User user = msGraph.getAdUser(id);
        AdUser aduser = new AdUser();
        List<Group> groups = msGraph.groupsOf(id);
        model.addAttribute("user", user);
        model.addAttribute("groups", groups);
        model.addAttribute("aduser", aduser);
        return "deleteGroup";
    }

    
    /** 
     * @param requestParams
     * @param user
     * @param Model
     * @return String
     * @throws IOException
     */
    @PostMapping("/deleteGroup{id}")
    public String deleteGroupP(@RequestParam Map<String, String> requestParams, @ModelAttribute("user") AdUser user,
            Model Model) throws IOException {
        Graph msGraph = new Graph();
        String id = requestParams.get("id");
        for (int i = 0; i < user.getGroups().size(); i++) {
            msGraph.deleteFromGroup(id, user.getGroups().get(i));
        }
        return "redirect:/users/" + id;
    }

    
    /** 
     * @param id
     * @param model
     * @return String
     * @throws IOException
     */
    @GetMapping("/giveRole/{id}")
    public String giveRole(@PathVariable(value = "id") String id, Model model) throws IOException {
        Graph msGraph = new Graph();
        User user = msGraph.getAdUser(id);
        AdUser aduser = new AdUser();
        Set<DirectoryRole> roles = msGraph.NotHaveRoleList(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        model.addAttribute("aduser", aduser);
        return "giveRole";
    }

    
    /** 
     * @param requestParams
     * @param user
     * @param Model
     * @return String
     * @throws IOException
     */
    @PostMapping("/giveRole{id}")
    public String giveRoleP(@RequestParam Map<String, String> requestParams, @ModelAttribute("user") AdUser user,
            Model Model) throws IOException {
        Graph msGraph = new Graph();
        String id = requestParams.get("id");
        for (int i = 0; i < user.getRoles().size(); i++) {
            msGraph.grantRole(user.getRoles().get(i), id);
        }
        return "redirect:/users/" + id;
    }

    
    /** 
     * @param id
     * @param model
     * @return String
     * @throws IOException
     */
    @GetMapping("/deleteRole/{id}")
    public String deleteRole(@PathVariable(value = "id") String id, Model model) throws IOException {
        Graph msGraph = new Graph();
        User user = msGraph.getAdUser(id);
        AdUser aduser = new AdUser();
        List<DirectoryRole> roles = msGraph.GetAllRoleFrom(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        model.addAttribute("aduser", aduser);
        return "deleteRole";
    }

    
    /** 
     * @param requestParams
     * @param user
     * @param Model
     * @return String
     * @throws IOException
     */
    @PostMapping("/deleteRole{id}")
    public String deleteRoleP(@RequestParam Map<String, String> requestParams, @ModelAttribute("user") AdUser user,
            Model Model) throws IOException {
        Graph msGraph = new Graph();
        String id = requestParams.get("id");
        for (int i = 0; i < user.getRoles().size(); i++) {
            msGraph.deleteRoleFrom(user.getRoles().get(i), id);
        }
        return "redirect:/users/" + id;
    }

    
    /** 
     * @param model
     * @return String
     * @throws IOException
     * @throws SQLException
     */
    @GetMapping("/scopes")
    public String scopePage(Model model) throws IOException, SQLException {
        DbConnect db = new DbConnect();
        Scope scope = new Scope();
        model.addAttribute("selectedScope", scope);
        List<Scope> scopeList = db.getScopeList();
        model.addAttribute("scopes", scopeList);
        return "scopes";
    }

    
    /** 
     * @param Model
     * @param scope
     * @return String
     * @throws IOException
     * @throws SQLException
     */
    @PostMapping("/scopes")
    public String scopePageP(Model Model, @ModelAttribute("selectedScope") Scope scope)
            throws IOException, SQLException {

        DbConnect db = new DbConnect();
        Scope newScope = db.getScope(scope.getScopeName());
        System.out.println(newScope.getScopeName());
        newScope.writeInFile();
        return "redirect:/index";
    }

    
    /** 
     * @param model
     * @return String
     * @throws IOException
     */
    @GetMapping("/addScope")
    public String addScopeFrom(Model model) throws IOException {
        Scope scope = new Scope(0);
        model.addAttribute("scope", scope);
        return "addScope";
    }

    
    /** 
     * @param model
     * @param scope
     * @return String
     * @throws IOException
     * @throws SQLException
     */
    @PostMapping("/addScope")
    public String addScopeP(Model model, @ModelAttribute("scope") Scope scope) throws IOException, SQLException {
        DbConnect db = new DbConnect();
        db.addScope(scope.getTenantId(), scope.getPassword(), scope.getScopeName(), scope.getAppId());
        return "redirect:/scopes";
    }

    
    /** 
     * @param model
     * @return String
     * @throws IOException
     * @throws SQLException
     */
    @GetMapping("/adChanges")
    public String adChanges(Model model) throws IOException, SQLException {
        ChangeDetect detect = new ChangeDetect();
    List<AdChanges> changes = new ArrayList<AdChanges>();
        changes = detect.changesList();
        ChangesWrapper wrap = new ChangesWrapper();
        wrap.setChangesList(changes);
        model.addAttribute("wrapper", wrap);
        return "adChanges";
    }

    
    /** 
     * @param model
     * @param Viewlist
     * @param bindingResult
     * @return String
     * @throws IOException
     * @throws SQLException
     * @throws ParseException
     */
    @PostMapping("/adChanges")
    public String adChangesP(Model model, @ModelAttribute("wrapper") ChangesWrapper Viewlist,
            BindingResult bindingResult) throws IOException, SQLException, ParseException {
        DbConnect db=new DbConnect();
        ChangeDetect detect = new ChangeDetect();
        List<AdChanges> list = new ArrayList<AdChanges>();
        list = Viewlist.getChangesList();
        detect.applyAllChanges(list);
        db.refreshDb();
        return "redirect:/adChanges";
    }

    
    /** 
     * @param model
     * @return String
     * @throws IOException
     * @throws SQLException
     */
    @GetMapping("/history")
    public String history(Model model) throws IOException, SQLException {
        DbConnect db = new DbConnect();
        List<History> list = new ArrayList<History>();
        list = db.getHistoryList();
        model.addAttribute("list", list);
        return "history";
    }

    
    /** 
     * @param id
     * @param model
     * @return String
     * @throws IOException
     * @throws SQLException
     */
    @GetMapping("/history/{id}")
    public String historyDetail(@PathVariable(value = "id") String id, Model model) throws IOException, SQLException {
        DbConnect db = new DbConnect();
        List<AdChanges> list = new ArrayList<AdChanges>();
        list = db.getChangesList(Integer.parseInt(id));
        model.addAttribute("list", list);
        model.addAttribute("id", id);
        return "historyDetail";
    }

    
    /** 
     * @param requestParams
     * @return String
     * @throws IOException
     * @throws NumberFormatException
     * @throws SQLException
     */
    @PostMapping("/deleteHistory")
    public String deleteHistory(@RequestParam Map<String, String> requestParams)
            throws IOException, NumberFormatException, SQLException {
        DbConnect db = new DbConnect();
        db.deleteHistory(Integer.parseInt(requestParams.get("id")));
        return "redirect:/history";
    }

    @PostMapping("/refreshdb")
    public void refreshdb() throws IOException, SQLException, ParseException{
        DbConnect db= new DbConnect();
        db.refreshDb();
    }
}