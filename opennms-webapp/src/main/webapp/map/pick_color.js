var x_picker,pick_begin,num_color_picker,pick_color,pick_appui,comp_rouge,comp_vert,comp_bleu,pick_color,pick_prefixe,node,cible;

function add_pick_color(file,noeud,num)
{pick_appui=false;cible="";num_color_picker=num;
comp_rouge=128;comp_vert=128;comp_bleu=128;pick_color="#808080";
pick_prefixe=noeud;pick_begin=true;
node=menuSvgDocument.getElementById(noeud);
getURL (file, pick_active);
};

function pick_active( data ) {
if (data.success) {
var doc_frag = parseXML ( data.content, menuSvgDocument);
childs=doc_frag.getChildNodes();
nombre=childs.getLength();
for (i=0;i<nombre;i++)
{child=childs.item(i);
node.appendChild (child)
};
} else {
alert ("Error reading file");
}}

function remove_pick_color(noeud)
{
var ou=menuSvgDocument.getElementById(noeud);
childs=ou.getChildNodes();
if(childs!=null){
nombre=childs.getLength();
for (i=nombre-1;i>=0;i--)
{child=childs.item(i);
ou.removeChild (child)
}
}
}

function pick_choisir(evt)
{cible=evt.getTarget().getAttribute("id");
if ((cible=="rouge")||(cible=="vert")||(cible=="bleu"))
{if (pick_begin==true)
{pick_begin=false;x_picker=evt.getClientX()-75};
pick_appui=true}}

function pick_bouger(evt)
{if (pick_appui==true)
{var xcurs=evt.getClientX()-x_picker;if (xcurs<10) {xcurs=10};if (xcurs>140) {xcurs=140};
var obj=menuSvgDocument.getElementById(cible);
obj.setAttribute("x",xcurs);
compo=parseInt((xcurs-10)*255/130);
if (cible=="rouge") {comp_rouge=compo};
if (cible=="vert") {comp_vert=compo};
if (cible=="bleu") {comp_bleu=compo};
if (comp_rouge<16) {pick_color="#0"+comp_rouge.toString(16)} else {pick_color="#"+comp_rouge.toString(16)};
if (comp_vert<16) {pick_color=pick_color+"0"+comp_vert.toString(16)} else {pick_color=pick_color+comp_vert.toString(16)}; 
if (comp_bleu<16) {pick_color=pick_color+"0"+comp_bleu.toString(16)} else {pick_color=pick_color+comp_bleu.toString(16)};
obj=menuSvgDocument.getElementById("test");obj.getStyle().setProperty("fill",pick_color);
/*node=mapSvgDocument.getElementById("couleur");child=node.getFirstChild();
child.setData(pick_color);*/

}}

function pick_lacher(evt)
{pick_appui=false}

function pick_color_tape(evt)
{key = evt.getCharCode();
if (key==13) {close_pick_color(true)};
if (key==27) {close_pick_color(false)};
}

function close_pick_color(garder)
{
if(pick_prefixe!=null){remove_pick_color(pick_prefixe);
eval(pick_prefixe+"_use_pick_color(garder,pick_color,num_color_picker)");
}
}
