<#import "../../_layouts/_default.ftl" as layout>
<#import "../../_components/_button.ftl" as c>

<@layout.page>
  <h1 class="text-primary">¡Verifica tu dirección de correo!</h1>
  <p class="text-secondary">Hola ${name},</p>
  <p class="text-secondary">
    Gracias por registrarte. Por favor, haz clic en el botón de abajo para
    verificar tu cuenta.
  </p>

  <br />

  <@c.primary link=verificationUrl text="Verificar mi cuenta" />

  <br />

  <p class="text-secondary">
    Si el botón no funciona, copia y pega esta URL en tu navegador:<br />
    <a href="${verificationUrl}" class="link">${verificationUrl}</a>
  </p>
</@layout.page>
