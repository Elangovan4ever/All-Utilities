﻿<Global.Microsoft.VisualBasic.CompilerServices.DesignerGenerated()>
Partial Class Form2
    Inherits System.Windows.Forms.Form

    'Form overrides dispose to clean up the component list.
    <System.Diagnostics.DebuggerNonUserCode()>
    Protected Overrides Sub Dispose(ByVal disposing As Boolean)
        Try
            If disposing AndAlso components IsNot Nothing Then
                components.Dispose()
            End If
        Finally
            MyBase.Dispose(disposing)
        End Try
    End Sub

    'Required by the Windows Form Designer
    Private components As System.ComponentModel.IContainer

    'NOTE: The following procedure is required by the Windows Form Designer
    'It can be modified using the Windows Form Designer.  
    'Do not modify it using the code editor.
    <System.Diagnostics.DebuggerStepThrough()>
    Private Sub InitializeComponent()
        Me.components = New System.ComponentModel.Container()
        Dim resources As System.ComponentModel.ComponentResourceManager = New System.ComponentModel.ComponentResourceManager(GetType(Form2))
        Me.ImageList1 = New System.Windows.Forms.ImageList(Me.components)
        Me.ElaCustomTab1 = New ElaCustomTab.ElaCustomTab()
        Me.TabPage1 = New System.Windows.Forms.TabPage()
        Me.TabPage2 = New System.Windows.Forms.TabPage()
        Me.ElaCustomTab1.SuspendLayout()
        Me.SuspendLayout()
        '
        'ImageList1
        '
        Me.ImageList1.ImageStream = CType(resources.GetObject("ImageList1.ImageStream"), System.Windows.Forms.ImageListStreamer)
        Me.ImageList1.TransparentColor = System.Drawing.Color.Transparent
        Me.ImageList1.Images.SetKeyName(0, "bills.png")
        Me.ImageList1.Images.SetKeyName(1, "customer.png")
        Me.ImageList1.Images.SetKeyName(2, "design.png")
        Me.ImageList1.Images.SetKeyName(3, "help.png")
        Me.ImageList1.Images.SetKeyName(4, "payment.png")
        Me.ImageList1.Images.SetKeyName(5, "report.png")
        Me.ImageList1.Images.SetKeyName(6, "settings.png")
        '
        'ElaCustomTab1
        '
        Me.ElaCustomTab1.Controls.Add(Me.TabPage1)
        Me.ElaCustomTab1.Controls.Add(Me.TabPage2)
        Me.ElaCustomTab1.Location = New System.Drawing.Point(133, 29)
        Me.ElaCustomTab1.Name = "ElaCustomTab1"
        Me.ElaCustomTab1.SelectedIndex = 0
        Me.ElaCustomTab1.Size = New System.Drawing.Size(571, 336)
        Me.ElaCustomTab1.TabIndex = 0
        '
        'TabPage1
        '
        Me.TabPage1.Location = New System.Drawing.Point(4, 22)
        Me.TabPage1.Name = "TabPage1"
        Me.TabPage1.Padding = New System.Windows.Forms.Padding(3)
        Me.TabPage1.Size = New System.Drawing.Size(563, 310)
        Me.TabPage1.TabIndex = 0
        Me.TabPage1.Text = "TabPage1"
        Me.TabPage1.UseVisualStyleBackColor = True
        '
        'TabPage2
        '
        Me.TabPage2.Location = New System.Drawing.Point(4, 22)
        Me.TabPage2.Name = "TabPage2"
        Me.TabPage2.Padding = New System.Windows.Forms.Padding(3)
        Me.TabPage2.Size = New System.Drawing.Size(192, 74)
        Me.TabPage2.TabIndex = 1
        Me.TabPage2.Text = "TabPage2"
        Me.TabPage2.UseVisualStyleBackColor = True
        '
        'Form2
        '
        Me.AutoScaleDimensions = New System.Drawing.SizeF(6.0!, 13.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.ClientSize = New System.Drawing.Size(1126, 552)
        Me.Controls.Add(Me.ElaCustomTab1)
        Me.Name = "Form2"
        Me.Text = "Form2"
        Me.ElaCustomTab1.ResumeLayout(False)
        Me.ResumeLayout(False)

    End Sub
    Friend WithEvents ImageList1 As ImageList
    Friend WithEvents ElaCustomTab1 As ElaCustomTab.ElaCustomTab
    Friend WithEvents TabPage1 As TabPage
    Friend WithEvents TabPage2 As TabPage
End Class
