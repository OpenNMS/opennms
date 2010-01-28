function add_pick_color(noeud)
{
resetPick();
pick_prefixe=noeud;
node=document.getElementById(noeud);
};

function resetPick(){
pick_appui=false;cible="";
comp_rouge=128;comp_vert=128;comp_bleu=128;pick_color="#808080";
pick_begin=true;
var rect=document.getElementById("rouge");
rect.setAttributeNS(null,"x",75);
rect=document.getElementById("vert");
rect.setAttributeNS(null,"x",75);
rect=document.getElementById("bleu");
rect.setAttributeNS(null,"x",75);
rect=document.getElementById("test");
rect.setAttributeNS(null,"style","fill:"+pick_color);

}

function pick_active( data ) {
if (data.success) {
var doc_frag = parseXML ( data.content, document);
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
var ou=document.getElementById(noeud);
if(ou!=undefined)
	ou.setAttributeNS(null,"display","none")
}

function pick_choisir(evt)
{cible=evt.target.getAttributeNS(null,"id");
if ((cible=="rouge")||(cible=="vert")||(cible=="bleu"))
{if (pick_begin==true)
{pick_begin=false;x_picker=evt.clientX-75};
pick_appui=true}}

function pick_bouger(evt)
{
if (pick_appui==true)
{
var xcurs=evt.clientX-x_picker;if (xcurs<10) {xcurs=10};if (xcurs>140) {xcurs=140};
var obj=document.getElementById(cible);
obj.setAttributeNS(null,"x",xcurs);
compo=parseInt((xcurs-10)*255/130);
if (cible=="rouge") {comp_rouge=compo};
if (cible=="vert") {comp_vert=compo};
if (cible=="bleu") {comp_bleu=compo};
if (comp_rouge<16) {pick_color="#0"+comp_rouge.toString(16)} else {pick_color="#"+comp_rouge.toString(16);};
if (comp_vert<16) {pick_color=pick_color+"0"+comp_vert.toString(16)} else {pick_color=pick_color+comp_vert.toString(16)}; 
if (comp_bleu<16) {pick_color=pick_color+"0"+comp_bleu.toString(16)} else {pick_color=pick_color+comp_bleu.toString(16)};
obj=document.getElementById("test");
obj.setAttributeNS(null,"style","fill:"+pick_color);

}}

function pick_lacher(evt)
{pick_appui=false}

function pick_color_tape(evt)
{key = evt.getCharCode();
if (key==13) {close_pick_color(true)};
if (key==27) {close_pick_color(false)};
}

