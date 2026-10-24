document.querySelectorAll('.account-trigger').forEach(button=>button.addEventListener('click',()=>button.closest('.account-menu').classList.toggle('open')));
document.addEventListener('click',event=>document.querySelectorAll('.account-menu.open').forEach(menu=>{if(!menu.contains(event.target))menu.classList.remove('open')}));
