<#import "../../_layouts/_default.ftl" as layout>
<#import "../../_components/_button.ftl" as c>

<@layout.page>
  <h1 class="text-primary">Solicitud de restablecimiento de contraseña</h1>
  <p class="text-secondary">Hola ${name},</p>
  <p class="text-secondary">
    Hemos recibido una solicitud para restablecer tu contraseña en tu cuenta de ${appName}. Si no realizaste esta solicitud, puedes ignorar este correo electrónico.<br />
    De lo contrario, haz clic en el botón de abajo para restablecer tu contraseña.
  </p>

  <br />

  <@c.primary link=passwordResetUrl text="Restablecer contraseña" />

  <br />

  <p class="text-secondary">
    Si el botón no funciona, copia y pega esta URL en tu navegador:<br />
    <a href="${passwordResetUrl}" class="link">${passwordResetUrl}</a>
  </p>
</@layout.page>
