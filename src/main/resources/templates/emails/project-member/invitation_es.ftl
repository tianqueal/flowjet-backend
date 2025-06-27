<#import "../../_layouts/_default.ftl" as layout>
<#import "../../_components/_button.ftl" as c>

<@layout.page>
  <h1 class="text-primary">Has sido invitado a un proyecto</h1>
  <p class="text-secondary">Hola ${name},</p>
  <p class="text-secondary">
    Has sido invitado a unirte al proyecto <strong>${projectName}</strong> en <strong>${appName}</strong>.
    Por favor, haz clic en el botón de abajo para aceptar la invitación y unirte al proyecto.
  </p>

  <br />

  <@c.primary link=projectMemberInvitationUrl text="Aceptar invitación" />

  <br />

  <p class="text-secondary">
    Si el botón no funciona, copia y pega esta URL en tu navegador:<br />
    <a href="${projectMemberInvitationUrl}" class="link">${projectMemberInvitationUrl}</a>
  </p>
</@layout.page>
