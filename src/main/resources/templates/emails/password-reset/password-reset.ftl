<#import "../../_layouts/_default.ftl" as layout>
<#import "../../_components/_button.ftl" as c>

<@layout.page>
  <h1 class="text-primary">Password Reset Request</h1>
  <p class="text-secondary">Hi ${name},</p>
  <p class="text-secondary">
    We received a request to reset your password for your account on ${appName}. If you did not make this request, you can ignore this email.<br />
    Otherwise, please click the button below to reset your password.
  </p>

  <br />

  <@c.primary link=passwordResetUrl text="Reset Password" />

  <br />

  <p class="text-secondary">
    If the button doesn't work, copy and paste this URL into your browser:<br />
    <a href="${passwordResetUrl}" class="link">${passwordResetUrl}</a>
  </p>
</@layout.page>
