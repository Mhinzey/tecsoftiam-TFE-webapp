function rowClicked(value) {
    location.href = '/users/' + value;
}

function giveRole(value) {
    location.href = '/giveRole/'+ value;
}

function deleteRole(value) {
    location.href = '/deleteRole/'+ value;
}
function giveGroup(value) {
    location.href = '/giveGroup/'+ value;
}

function deleteGroup(value) {
    location.href = '/deleteGroup/'+ value;
}
function myValidationFunction(){
    
    if((confirm("Etes-vous sur de vouloir supprimer l'utilisateur?") == true)){
     $("#deleteUser").submit(); 
    }
  else{
    event.preventDefault();
    return}
  
  }

  function registervalidation(){
 
    var pass1=$('#passw1').val();
    var pass2=$('#passw2').val();
    if($('#displayname').val() == '' || $('#nickname').val() == '' || $('#passw1').val() == '' || $('#passw2').val() == ''  ){
        alert('Les champs doivent être remplis');
        event.preventDefault();
    return
     }
     if(pass1 !== pass2){
        alert('Les mots de passe ne sont pas identiques');
        event.preventDefault();
    return
     }
     if(checkPassword(pass1) ===false ){
        alert('Le mot de passe ne correspond pas aux critères minimums');
        event.preventDefault();
    return
     }
     else{
        $("#registerform").submit(); 
     }
  }

  function checkPassword(value){
    var hasUpper=false;
    var hasLower=false;
    var hasChracter=false;
    var hasNumber=false;
    var i=0;
    var charact='';
    var format = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]+/;
    while (i <= value.length){
        character = value.charAt(i);
        if (!isNaN(character * 1)){
            hasNumber=true;
        }else{
            if (character == character.toUpperCase()) {
                hasUpper=true;
            }
            if (character == character.toLowerCase()){
                hasLower=true;
            }
            if(format.test(value)){
                hasChracter=true;
            }
        }
        i++;
    }
    if(value.length<8|| value.length>256){
        return false;
    }
    if([hasChracter,hasLower,hasNumber,hasUpper].filter(Boolean).length < 3){
        return false;
    }
    else{
        return true;
    }
  }