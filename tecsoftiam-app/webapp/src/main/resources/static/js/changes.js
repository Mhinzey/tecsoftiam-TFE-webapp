

function myValidationFunction(){
    
    if((confirm("Etes vous sûr de vouloir rafraichir la base de données?") == true)){
     $("#refreshdb").submit(); 
     alert('Base de données mise à jour');
    }
  else{
    event.preventDefault();
    return}
  
  }

  function changeValidation(){
    
    if((confirm("Etes vous sûr de vouloir valider ces changements?") == true)){
     $("#changeForm").submit(); 
     alert('Changements validés et rapport créé');
    }
  else{
    event.preventDefault();
    return}
  
  }