<#import "../../_layouts/_default.ftl" as layout>
<#import "../../_components/_button.ftl" as c>

<@layout.page>
  <h1 class="text-primary">Verify Your Email Address!</h1>
  <p class="text-secondary">Hi ${name},</p>
  <p class="text-secondary">
    Thanks for signing up. Please click the button below to verify your account.
  </p>

  <br />

  <@c.primary link=verificationUrl text="Verify My Account" />

  <br />

  <p class="text-secondary">
    If the button doesn't work, copy and paste this URL into your browser:<br />
    <a href="${verificationUrl}" class="link">${verificationUrl}</a>
  </p>
</@layout.page>
