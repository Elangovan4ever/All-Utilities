<Global.Microsoft.VisualBasic.CompilerServices.DesignerGenerated()>
Partial Class Form1
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
        Me.ElaTextBoxWithCustomBorderColor1 = New CustomTextBoxBorder.ElaTextBoxWithCustomBorderColor()
        Me.ElaTextBoxWithCustomBorderColor2 = New CustomTextBoxBorder.ElaTextBoxWithCustomBorderColor()
        Me.ElaTextBoxWithCustomBorderColor3 = New CustomTextBoxBorder.ElaTextBoxWithCustomBorderColor()
        Me.ElaTextBoxWithCustomBorderColor4 = New CustomTextBoxBorder.ElaTextBoxWithCustomBorderColor()
        Me.ElaTextBoxWithCustomBorderColor5 = New CustomTextBoxBorder.ElaTextBoxWithCustomBorderColor()
        Me.SuspendLayout()
        '
        'ElaTextBoxWithCustomBorderColor1
        '
        Me.ElaTextBoxWithCustomBorderColor1.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaTextBoxWithCustomBorderColor1.BorderColorFocused = System.Drawing.Color.Orange
        Me.ElaTextBoxWithCustomBorderColor1.Location = New System.Drawing.Point(234, 82)
        Me.ElaTextBoxWithCustomBorderColor1.Name = "ElaTextBoxWithCustomBorderColor1"
        Me.ElaTextBoxWithCustomBorderColor1.Size = New System.Drawing.Size(314, 20)
        Me.ElaTextBoxWithCustomBorderColor1.TabIndex = 0
        '
        'ElaTextBoxWithCustomBorderColor2
        '
        Me.ElaTextBoxWithCustomBorderColor2.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaTextBoxWithCustomBorderColor2.BorderColorFocused = System.Drawing.Color.Orange
        Me.ElaTextBoxWithCustomBorderColor2.Location = New System.Drawing.Point(234, 161)
        Me.ElaTextBoxWithCustomBorderColor2.Name = "ElaTextBoxWithCustomBorderColor2"
        Me.ElaTextBoxWithCustomBorderColor2.Size = New System.Drawing.Size(314, 20)
        Me.ElaTextBoxWithCustomBorderColor2.TabIndex = 1
        '
        'ElaTextBoxWithCustomBorderColor3
        '
        Me.ElaTextBoxWithCustomBorderColor3.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaTextBoxWithCustomBorderColor3.BorderColorFocused = System.Drawing.Color.Orange
        Me.ElaTextBoxWithCustomBorderColor3.Location = New System.Drawing.Point(234, 122)
        Me.ElaTextBoxWithCustomBorderColor3.Name = "ElaTextBoxWithCustomBorderColor3"
        Me.ElaTextBoxWithCustomBorderColor3.Size = New System.Drawing.Size(314, 20)
        Me.ElaTextBoxWithCustomBorderColor3.TabIndex = 2
        '
        'ElaTextBoxWithCustomBorderColor4
        '
        Me.ElaTextBoxWithCustomBorderColor4.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaTextBoxWithCustomBorderColor4.BorderColorFocused = System.Drawing.Color.Orange
        Me.ElaTextBoxWithCustomBorderColor4.Location = New System.Drawing.Point(234, 242)
        Me.ElaTextBoxWithCustomBorderColor4.Name = "ElaTextBoxWithCustomBorderColor4"
        Me.ElaTextBoxWithCustomBorderColor4.Size = New System.Drawing.Size(314, 20)
        Me.ElaTextBoxWithCustomBorderColor4.TabIndex = 3
        '
        'ElaTextBoxWithCustomBorderColor5
        '
        Me.ElaTextBoxWithCustomBorderColor5.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaTextBoxWithCustomBorderColor5.BorderColorFocused = System.Drawing.Color.Orange
        Me.ElaTextBoxWithCustomBorderColor5.Location = New System.Drawing.Point(234, 202)
        Me.ElaTextBoxWithCustomBorderColor5.Name = "ElaTextBoxWithCustomBorderColor5"
        Me.ElaTextBoxWithCustomBorderColor5.Size = New System.Drawing.Size(314, 20)
        Me.ElaTextBoxWithCustomBorderColor5.TabIndex = 4
        '
        'Form1
        '
        Me.AutoScaleDimensions = New System.Drawing.SizeF(6.0!, 13.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.BackColor = System.Drawing.Color.White
        Me.ClientSize = New System.Drawing.Size(1064, 553)
        Me.Controls.Add(Me.ElaTextBoxWithCustomBorderColor5)
        Me.Controls.Add(Me.ElaTextBoxWithCustomBorderColor4)
        Me.Controls.Add(Me.ElaTextBoxWithCustomBorderColor3)
        Me.Controls.Add(Me.ElaTextBoxWithCustomBorderColor2)
        Me.Controls.Add(Me.ElaTextBoxWithCustomBorderColor1)
        Me.Name = "Form1"
        Me.Text = "Custom TextBox Border Color"
        Me.WindowState = System.Windows.Forms.FormWindowState.Maximized
        Me.ResumeLayout(False)
        Me.PerformLayout()

    End Sub

    Friend WithEvents ElaTextBoxWithCustomBorderColor1 As ElaTextBoxWithCustomBorderColor
    Friend WithEvents ElaTextBoxWithCustomBorderColor2 As ElaTextBoxWithCustomBorderColor
    Friend WithEvents ElaTextBoxWithCustomBorderColor3 As ElaTextBoxWithCustomBorderColor
    Friend WithEvents ElaTextBoxWithCustomBorderColor4 As ElaTextBoxWithCustomBorderColor
    Friend WithEvents ElaTextBoxWithCustomBorderColor5 As ElaTextBoxWithCustomBorderColor
End Class
