<#import "../../_layouts/_default.ftl" as layout>
<#import "../../_components/_button.ftl" as c>

<@layout.page>
  <h1 class="text-primary">You have been invited to join a project</h1>
  <p class="text-secondary">Hi ${name},</p>
  <p class="text-secondary">
    You have been invited to join the project <strong>${projectName}</strong> on <strong>${appName}</strong>.
    Please click the button below to accept the invitation and join the project.
  </p>

  <br />

  <@c.primary link=projectMemberInvitationUrl text="Accept Invitation" />

  <br />

  <p class="text-secondary">
    If the button doesn't work, copy and paste this URL into your browser:<br />
    <a href="${projectMemberInvitationUrl}" class="link">${projectMemberInvitationUrl}</a>
  </p>
</@layout.page>
